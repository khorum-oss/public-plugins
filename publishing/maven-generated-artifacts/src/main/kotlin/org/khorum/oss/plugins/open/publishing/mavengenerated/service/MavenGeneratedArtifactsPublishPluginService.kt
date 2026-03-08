package org.khorum.oss.plugins.open.publishing.mavengenerated.service

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.get
import org.khorum.oss.plugins.open.publishing.mavengenerated.domain.ManualMavenArtifactsExtension
import java.io.File
import org.gradle.plugins.signing.SigningExtension
import java.security.MessageDigest

open class MavenGeneratedArtifactsPublishPluginService :
    BuildService<MavenGeneratedArtifactsPublishPluginService.Params> {
    interface Params : BuildServiceParameters

    override fun getParameters(): Params = object : Params {}

    fun apply(project: Project): Project = project.run {
        pluginManager.apply("java")
        pluginManager.apply("maven-publish")

        val extension = project.extensions.create<ManualMavenArtifactsExtension>("mavenGeneratedArtifacts")

        project.afterEvaluate {
            val sourcesJar: TaskProvider<Jar> = createSourcesJar()

            val (dokkaJavadocJar, dokkaHtmlJar) = createDokkaTasksIfPluginEnabled(extension)

            val jarTasks: MutableList<TaskProvider<Jar>> = nonNullMutableList(sourcesJar, dokkaJavadocJar, dokkaHtmlJar)

            val hasSigning = configurePom(extension, artifactProviders = jarTasks)

            val generateHashTask = addGenerateHashesTask()

            addAssembleMavenArtifactsTask(
                extension.publicationName,
                dependsOn = jarTasks,
                finalizedBy = generateHashTask,
                hasSigning = hasSigning
            )
        }

        return project
    }

    private fun Project.createSourcesJar(): TaskProvider<Jar> {
        val sourceSets = extensions.getByType<SourceSetContainer>()

        return tasks.register<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }
    }

    private fun Project.createDokkaTasksIfPluginEnabled(
        extension: ManualMavenArtifactsExtension
    ): Pair<TaskProvider<Jar>?, TaskProvider<Jar>?> {
        var dokkaJavadocJar: TaskProvider<Jar>? = null
        var dokkaHtmlJar: TaskProvider<Jar>? = null

        if (extension.withDokka) {
            pluginManager.apply("org.jetbrains.dokka")

            dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
                archiveClassifier.set("javadoc")
                val dokkaTask = tasks.named("dokkaJavadoc")
                dependsOn(dokkaTask)
                from(dokkaTask.map { it.outputs.files })
            }

            dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
                archiveClassifier.set("kdoc")
                val dokkaTask = tasks.named("dokkaHtml")
                dependsOn(dokkaTask)
                from(dokkaTask.map { it.outputs.files })
            }
        }

        return dokkaJavadocJar to dokkaHtmlJar
    }

    private fun Project.configurePom(
        extension: ManualMavenArtifactsExtension,
        artifactProviders: List<TaskProvider<Jar>>
    ): Boolean {
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>(extension.publicationName) {
                    from(components["java"])
                    artifactProviders.forEach { artifact ->
                        artifact(artifact)
                    }

                    pom {
                        packaging = "jar"
                        name.set(extension.name)
                        description.set(extension.description?.trimIndent())
                        url.set(extension.websiteUrl)

                        licenses {
                            extension.licenses()?.forEach { license ->
                                license {
                                    name.set(license.name)
                                    url.set(license.url)
                                }
                            }
                        }
                        developers {
                            extension.developers()?.forEach { developer ->
                                developer {
                                    id.set(developer.id)
                                    name.set(developer.name)
                                    email.set(developer.email)
                                    organization.set(developer.organization)
                                }
                            }
                        }
                        scm {
                            val scm = extension.scm()
                            val connectionLocation = scm?.connection?.getOrNull() ?: extension.websiteUrl
                            val developerConnectionLocation = scm?.developerConnection ?: connectionLocation
                            connection.set("scm:git:git://$connectionLocation")
                            developerConnection.set("scm:git:ssh://$developerConnectionLocation")
                            url.set(scm?.url ?: extension.websiteUrl)
                        }

                        withXml {
                            sanitizePomXml(asNode())
                        }
                    }
                }
            }
        }

        val signingKeyFile = project.rootProject.file("khorum-signing.asc")
        val signingPassword = (project.findProperty("signing.password") as? String)
            ?: System.getenv("GPG_SIGNING_PASSWORD")

        val signingKey = when {
            signingKeyFile.exists() -> {
                logger.lifecycle(" | [SIGNING] Found signing key file: ${signingKeyFile.absolutePath}")
                signingKeyFile.readText()
            }
            System.getenv("GPG_SIGNING_KEY") != null -> {
                logger.lifecycle(" | [SIGNING] Using signing key from GPG_SIGNING_KEY environment variable")
                System.getenv("GPG_SIGNING_KEY")
            }
            else -> {
                logger.lifecycle(" | [SIGNING] No signing key found (checked: ${signingKeyFile.absolutePath}, GPG_SIGNING_KEY env)")
                null
            }
        }

        if (signingKey != null && signingPassword != null) {
            logger.lifecycle(" | [SIGNING] Signing publication '${extension.publicationName}'")
            project.pluginManager.apply("signing")
            project.extensions.configure<SigningExtension> {
                useInMemoryPgpKeys(signingKey, signingPassword)
                sign(project.extensions.getByType<PublishingExtension>()
                    .publications[extension.publicationName])
            }
            return true
        } else if (extension.signingRequired) {
            val missing = listOfNotNull(
                if (signingKey == null) "signing key" else null,
                if (signingPassword == null) "signing password" else null,
            )
            throw org.gradle.api.GradleException(
                "Signing is required but missing: ${missing.joinToString(", ")}. " +
                "Set signingRequired = false in mavenGeneratedArtifacts { } to skip signing."
            )
        } else {
            logger.lifecycle(" | [SIGNING] Skipping signing (signingRequired = false)")
        }

        return false
    }

    private val MALFORMED_TAG_NAMES = mapOf(
        "n" to "name",
        "nam" to "name",
        "desc" to "description",
        "descriptio" to "description",
    )

    fun sanitizePomXml(node: groovy.util.Node) {
        val children = node.children().toList()
        for (child in children) {
            if (child is groovy.util.Node) {
                val nodeName = child.name().toString().let { raw ->
                    // Handle qualified names like {namespace}localPart
                    raw.substringAfterLast("}")
                        .substringAfterLast(":")
                        .ifEmpty { raw }
                }

                val correctedName = MALFORMED_TAG_NAMES[nodeName]
                if (correctedName != null) {
                    val value = child.text()
                    val parent = child.parent()
                    parent.remove(child)
                    parent.appendNode(correctedName, value)
                } else {
                    sanitizePomXml(child)
                }
            }
        }
    }

    fun <T> nonNullMutableList(vararg items: T?): MutableList<T> = sequenceOf(*items)
        .filterNotNull()
        .toMutableList()

    fun Project.addGenerateHashesTask(): TaskProvider<Task> {
        return tasks.register("generateHashes") {
            group = "distribution"
            description = "Generates SHA-256 and SHA-1 hash files for all artifacts."
            doLast {
                val libsDir = file("${layout.buildDirectory.get()}/libs")
                libsDir.listFiles()
                    ?.filter { it.isFile }
                    ?.filter { it.extension in listOf("jar", "war", "aar", "pom") }
                    ?.forEach { file ->
                        if (file.isFile) {
                            listOf("SHA-256", "SHA-1").forEach { algo ->
                                val hash = file.generateHash(algo)
                                val ext = when (algo) {
                                    "SHA-1" -> "sha1"
                                    "SHA-256" -> "sha256"
                                    else -> algo.lowercase()
                                }
                                file.resolveSibling("${file.name}.$ext").writeText(hash)
                                logger.lifecycle(" | [INFO] Created file: ${file.name}.$ext")
                            }
                        }
                    }
            }
        }
    }

    fun Project.addAssembleMavenArtifactsTask(
        publicationName: String = "maven",
        dependsOn: MutableList<TaskProvider<Jar>>,
        finalizedBy: TaskProvider<Task>,
        hasSigning: Boolean = false
    ) {
        val capitalizedPublicationName = publicationName.replaceFirstChar { it.uppercase() }

        tasks.register("assembleMavenArtifacts") {
            group = "distribution"
            description = "Builds main, sources, javadoc, kdoc jars and the POM."
            dependsOn("jar", "generatePomFileFor${capitalizedPublicationName}Publication")
            dependsOn(*dependsOn.toTypedArray())
            if (hasSigning) {
                dependsOn("sign${capitalizedPublicationName}Publication")
            }
            finalizedBy(finalizedBy)
        }
    }

    fun File.generateHash(hashAlgo: String): String {
        val buffer = ByteArray(1024 * 4)
        val md = MessageDigest.getInstance(hashAlgo)
        inputStream().use { fis ->
            var bytes = fis.read(buffer)
            while (bytes >= 0) {
                if (bytes > 0) md.update(buffer, 0, bytes)
                bytes = fis.read(buffer)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
