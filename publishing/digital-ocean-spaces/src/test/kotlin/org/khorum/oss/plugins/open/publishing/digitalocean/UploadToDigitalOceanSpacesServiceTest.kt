package org.khorum.oss.plugins.open.publishing.digitalocean

import io.mockk.*
import org.khorum.oss.plugins.open.publishing.digitalocean.adapter.ProjectAdapter
import org.khorum.oss.plugins.open.publishing.digitalocean.adapter.S3BuilderAdapter
import org.khorum.oss.plugins.open.publishing.digitalocean.client.DefaultDigitalOceanSpacesClient
import org.khorum.oss.plugins.open.publishing.digitalocean.client.DryRunDigitalOceanSpacesClient
import org.khorum.oss.plugins.open.publishing.digitalocean.domain.DigitalOceanSpacesExtension
import org.khorum.oss.plugins.open.publishing.digitalocean.domain.Namespace
import org.khorum.oss.plugins.open.publishing.digitalocean.service.CheckVersionDigitalOceanSpacesService
import org.khorum.oss.plugins.open.publishing.digitalocean.service.UploadToDigitalOceanSpacesService
import org.khorum.oss.test.core.UnitTest
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.io.File
import java.nio.file.Path

class UploadToDigitalOceanSpacesServiceTest : UnitTest() {
    private val logger: Logger = Logging.getLogger(UploadToDigitalOceanSpacesServiceTest::class.java)

    private val s3Client = mockk<S3Client> {
        every { close() } just Runs
    }

    private val s3BuilderAdapter = mockk<S3BuilderAdapter> {
        every { build() } returns s3Client
    }

    private val mockVersionCheck: CheckVersionDigitalOceanSpacesService = mockk()

    private val testBucket = "test-bucket"
    private val sharedAccessKey = "test-access"
    private val sharedSecretKey = "test-secret"

    private fun createExtension() = DigitalOceanSpacesExtension().apply {
        accessKey = sharedAccessKey
        secretKey = sharedSecretKey
        bucket = testBucket
    }

    private fun createClient(ext: DigitalOceanSpacesExtension = createExtension()) =
        DefaultDigitalOceanSpacesClient(ext, logger) { _, _ -> s3BuilderAdapter }

    private fun createDryRunClient(ext: DigitalOceanSpacesExtension = createExtension()) =
        DryRunDigitalOceanSpacesClient(ext, logger)

    private class TestProjectAdapter(
        tempDir: Path,
        override val name: String = "my-lib",
        override val group: String = "org.khorum.oss",
        override val version: String = "1.0.0"
    ) : ProjectAdapter {
        override val buildDir: File = tempDir.toFile()
        override val logger: Logger = Logging.getLogger(UploadToDigitalOceanSpacesServiceTest::class.java)

        override val project: Project = run {
            val buildDirFile = buildDir
            val dir = mockk<org.gradle.api.file.Directory>(relaxed = true)
            every { dir.asFile } returns buildDirFile

            val proj = mockk<Project>(relaxed = true)
            every { proj.layout.buildDirectory.get() } returns dir
            every { proj.file(any<String>()) } answers { File(arg<String>(0)) }
            proj
        }

        override fun pluginAdapters(): List<ProjectAdapter.MavenPublicationAdapter> = listOf(
            object : ProjectAdapter.MavenPublicationAdapter {
                override val groupId: String = this@TestProjectAdapter.group
                override val artifactId: String = this@TestProjectAdapter.name
                override val version: String = this@TestProjectAdapter.version
                override val name: String = "digitalOceanSpaces"
            }
        )
    }

    @Test
    fun `uploadToSpaces fails with clear error when main jar does not exist`(@TempDir tempDir: Path) {
        val project = TestProjectAdapter(tempDir)

        // Create everything EXCEPT the main jar
        createFileStructureWithoutMainJar(tempDir.toFile())

        val uploadService = UploadToDigitalOceanSpacesService(
            project,
            createDryRunClient(),
            s3Client,
            isPlugin = false,
            mockVersionCheck
        )

        every { mockVersionCheck.checkVersion(any(), any(), any()) } just Runs

        val exception = assertThrows<GradleException> {
            uploadService.uploadToSpaces()
        }

        assert(exception.message!!.contains("Main jar file not found")) {
            "Expected error about missing jar, got: ${exception.message}"
        }
        assert(exception.message!!.contains("my-lib-1.0.0.jar")) {
            "Expected error to mention expected jar name, got: ${exception.message}"
        }
    }

    @Test
    fun `uploadToSpaces error message lists available jars when main jar is missing`(@TempDir tempDir: Path) {
        val project = TestProjectAdapter(tempDir)

        // Create only sources jar (not the main jar)
        val libsDir = File(tempDir.toFile(), "libs").apply { mkdirs() }
        File(libsDir, "my-lib-1.0.0-sources.jar").writeText("dummy")

        val uploadService = UploadToDigitalOceanSpacesService(
            project,
            createDryRunClient(),
            s3Client,
            isPlugin = false,
            mockVersionCheck
        )

        every { mockVersionCheck.checkVersion(any(), any(), any()) } just Runs

        val exception = assertThrows<GradleException> {
            uploadService.uploadToSpaces()
        }

        assert(exception.message!!.contains("my-lib-1.0.0-sources.jar")) {
            "Expected error to list available jars, got: ${exception.message}"
        }
    }

    @Test
    fun `uploadToSpaces uploads main jar when it exists`(@TempDir tempDir: Path) {
        val project = TestProjectAdapter(tempDir)
        createFullFileStructure(tempDir.toFile())

        val uploadService = UploadToDigitalOceanSpacesService(
            project,
            createClient(),
            s3Client,
            isPlugin = false,
            mockVersionCheck
        )

        every { mockVersionCheck.checkVersion(any(), any(), any()) } just Runs
        every {
            s3Client.putObject(any<PutObjectRequest>(), any<Path>())
        } returns PutObjectResponse.builder().build()

        uploadService.uploadToSpaces()

        // Verify the main jar was uploaded
        verify {
            s3Client.putObject(
                match<PutObjectRequest> {
                    it.bucket() == testBucket
                        && it.key() == "org/khorum/oss/my-lib/1.0.0/my-lib-1.0.0.jar"
                        && it.acl() == ObjectCannedACL.PUBLIC_READ
                },
                match<Path> {
                    it.endsWith("libs/my-lib-1.0.0.jar")
                }
            )
        }
    }

    @Test
    fun `uploadToSpaces uploads all expected artifacts`(@TempDir tempDir: Path) {
        val project = TestProjectAdapter(tempDir)
        createFullFileStructure(tempDir.toFile())

        val uploadedKeys = mutableListOf<String>()
        val ext = createExtension()
        val client = DefaultDigitalOceanSpacesClient(ext, logger) { _, _ -> s3BuilderAdapter }

        val uploadService = UploadToDigitalOceanSpacesService(
            project,
            client,
            s3Client,
            isPlugin = false,
            mockVersionCheck
        )

        every { mockVersionCheck.checkVersion(any(), any(), any()) } just Runs
        every {
            s3Client.putObject(any<PutObjectRequest>(), any<Path>())
        } answers {
            val request = firstArg<PutObjectRequest>()
            uploadedKeys.add(request.key())
            PutObjectResponse.builder().build()
        }

        uploadService.uploadToSpaces()

        val basePath = "org/khorum/oss/my-lib/1.0.0"

        // Verify main jar and its hashes are uploaded
        assert("$basePath/my-lib-1.0.0.jar" in uploadedKeys) {
            "Main jar not uploaded. Uploaded keys: $uploadedKeys"
        }
        assert("$basePath/my-lib-1.0.0.jar.sha1" in uploadedKeys) {
            "Jar SHA1 not uploaded. Uploaded keys: $uploadedKeys"
        }
        assert("$basePath/my-lib-1.0.0.jar.sha256" in uploadedKeys) {
            "Jar SHA256 not uploaded. Uploaded keys: $uploadedKeys"
        }

        // Verify sources jar uploaded
        assert("$basePath/my-lib-1.0.0-sources.jar" in uploadedKeys) {
            "Sources jar not uploaded. Uploaded keys: $uploadedKeys"
        }

        // Verify javadoc jar uploaded
        assert("$basePath/my-lib-1.0.0-javadoc.jar" in uploadedKeys) {
            "Javadoc jar not uploaded. Uploaded keys: $uploadedKeys"
        }

        // Verify kdoc jar uploaded
        assert("$basePath/my-lib-1.0.0-kdoc.jar" in uploadedKeys) {
            "KDoc jar not uploaded. Uploaded keys: $uploadedKeys"
        }

        // Verify POM uploaded
        assert("$basePath/my-lib-1.0.0.pom" in uploadedKeys) {
            "POM not uploaded. Uploaded keys: $uploadedKeys"
        }
    }

    @Test
    fun `uploadToSpaces for plugin project uploads main jar and plugin jar`(@TempDir tempDir: Path) {
        val project = TestProjectAdapter(tempDir)
        createFullFileStructure(tempDir.toFile(), isPlugin = true)

        val uploadedKeys = mutableListOf<String>()
        val ext = createExtension()
        val client = DefaultDigitalOceanSpacesClient(ext, logger) { _, _ -> s3BuilderAdapter }

        val uploadService = UploadToDigitalOceanSpacesService(
            project,
            client,
            s3Client,
            isPlugin = true,
            mockVersionCheck
        )

        every { mockVersionCheck.checkVersion(any(), any(), any()) } just Runs
        every {
            s3Client.putObject(any<PutObjectRequest>(), any<Path>())
        } answers {
            val request = firstArg<PutObjectRequest>()
            uploadedKeys.add(request.key())
            PutObjectResponse.builder().build()
        }

        uploadService.uploadToSpaces()

        val basePath = "org/khorum/oss/my-lib/1.0.0"

        // Verify main jar uploaded
        assert("$basePath/my-lib-1.0.0.jar" in uploadedKeys) {
            "Main jar not uploaded. Uploaded keys: $uploadedKeys"
        }

        // Verify plugin jar uploaded
        val pluginPath = "org/khorum/oss/my-lib/org.khorum.oss.my-lib.gradle.plugin/1.0.0"
        assert("$pluginPath/org.khorum.oss.my-lib.gradle.plugin-1.0.0.jar" in uploadedKeys) {
            "Plugin jar not uploaded. Uploaded keys: $uploadedKeys"
        }

        // Verify plugin POM uploaded
        assert("$pluginPath/org.khorum.oss.my-lib.gradle.plugin-1.0.0.pom" in uploadedKeys) {
            "Plugin POM not uploaded. Uploaded keys: $uploadedKeys"
        }
    }

    @Test
    fun `namespace generates correct jar paths`() {
        val namespace = Namespace(
            groupId = "org.example",
            artifactId = "my-artifact",
            version = "2.0.0"
        )

        namespace.jarName eq "my-artifact-2.0.0.jar"
        namespace.jarPath eq "libs/my-artifact-2.0.0.jar"
        namespace.jarKey eq "org/example/my-artifact/2.0.0/my-artifact-2.0.0.jar"
        namespace.jarSha1Path eq "libs/my-artifact-2.0.0.jar.sha1"
        namespace.jarSha256Path eq "libs/my-artifact-2.0.0.jar.sha256"
    }

    @Test
    fun `namespace generates correct source jar paths`() {
        val namespace = Namespace(
            groupId = "org.example",
            artifactId = "my-artifact",
            version = "2.0.0"
        )

        namespace.sourcesJarName eq "my-artifact-2.0.0-sources.jar"
        namespace.sourcesJarPath eq "libs/my-artifact-2.0.0-sources.jar"
        namespace.sourcesJarKey eq "org/example/my-artifact/2.0.0/my-artifact-2.0.0-sources.jar"
    }

    @Test
    fun `namespace generates correct POM paths`() {
        val namespace = Namespace(
            groupId = "org.example",
            artifactId = "my-artifact",
            version = "2.0.0",
            publicationName = "digitalOceanSpaces"
        )

        namespace.sourcePomName eq "publications/digitalOceanSpaces/pom-default.xml"
        namespace.pomName eq "my-artifact-2.0.0.pom"
        namespace.pomPath eq "libs/my-artifact-2.0.0.pom"
        namespace.pomKey eq "org/example/my-artifact/2.0.0/my-artifact-2.0.0.pom"
    }

    @Test
    fun `namespace generates correct plugin paths with default pluginId`() {
        val namespace = Namespace(
            groupId = "org.example",
            artifactId = "my-plugin",
            version = "1.0.0"
        )

        namespace.pluginMarkerGroup eq "org.example.my-plugin"
        namespace.pluginMarkerArtifact eq "org.example.my-plugin.gradle.plugin"
        namespace.pluginFullPath eq "org/example/my-plugin/org.example.my-plugin.gradle.plugin/1.0.0"
        namespace.pluginJarName eq "org.example.my-plugin.gradle.plugin-1.0.0.jar"
        namespace.pluginPomName eq "org.example.my-plugin.gradle.plugin-1.0.0.pom"
    }

    @Test
    fun `namespace generates correct plugin paths with custom pluginId`() {
        val namespace = Namespace(
            groupId = "org.example",
            artifactId = "my-plugin",
            version = "1.0.0",
            pluginId = "org.example.custom.plugin.id"
        )

        namespace.pluginMarkerGroup eq "org.example.custom.plugin.id"
        namespace.pluginMarkerArtifact eq "org.example.custom.plugin.id.gradle.plugin"
        namespace.pluginFullPath eq "org/example/custom/plugin/id/org.example.custom.plugin.id.gradle.plugin/1.0.0"
    }

    private fun createFileStructureWithoutMainJar(buildDir: File) {
        val libsDir = File(buildDir, "libs").apply { mkdirs() }

        // Create secondary jars but NOT the main jar
        listOf("-sources", "-javadoc", "-kdoc").forEach { suffix ->
            File(libsDir, "my-lib-1.0.0$suffix.jar").writeText("dummy")
            File(libsDir, "my-lib-1.0.0$suffix.jar.sha1").writeText("dummy-sha1")
            File(libsDir, "my-lib-1.0.0$suffix.jar.sha256").writeText("dummy-sha256")
        }

        // Create POM
        File(libsDir, "my-lib-1.0.0.pom").writeText("<project/>")
        File(libsDir, "my-lib-1.0.0.pom.sha1").writeText("dummy-sha1")
        File(libsDir, "my-lib-1.0.0.pom.sha256").writeText("dummy-sha256")

        val publicationDir = File(buildDir, "publications/digitalOceanSpaces").apply { mkdirs() }
        File(publicationDir, "pom-default.xml").writeText("<project/>")
    }

    private fun createFullFileStructure(buildDir: File, isPlugin: Boolean = false) {
        val libsDir = File(buildDir, "libs").apply { mkdirs() }

        // Create ALL jars including the main jar
        listOf("", "-sources", "-javadoc", "-kdoc").forEach { suffix ->
            File(libsDir, "my-lib-1.0.0$suffix.jar").writeText("dummy jar content $suffix")
            File(libsDir, "my-lib-1.0.0$suffix.jar.sha1").writeText("dummy-sha1")
            File(libsDir, "my-lib-1.0.0$suffix.jar.sha256").writeText("dummy-sha256")
        }

        // Create POM in libs
        File(libsDir, "my-lib-1.0.0.pom").writeText("<project/>")
        File(libsDir, "my-lib-1.0.0.pom.sha1").writeText("dummy-sha1")
        File(libsDir, "my-lib-1.0.0.pom.sha256").writeText("dummy-sha256")

        // Create publication POM (source for copy)
        val publicationDir = File(buildDir, "publications/digitalOceanSpaces").apply { mkdirs() }
        File(publicationDir, "pom-default.xml").writeText(
            """<?xml version="1.0" encoding="UTF-8"?>
            <project>
                <groupId>org.khorum.oss</groupId>
                <artifactId>my-lib</artifactId>
                <version>1.0.0</version>
            </project>""".trimIndent()
        )
    }
}
