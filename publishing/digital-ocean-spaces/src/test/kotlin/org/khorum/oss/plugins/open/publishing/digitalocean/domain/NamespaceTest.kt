package org.khorum.oss.plugins.open.publishing.digitalocean.domain

import org.khorum.oss.test.core.UnitTest
import org.junit.jupiter.api.Test

class NamespaceTest : UnitTest() {

    @Test
    fun `test namespace creation`() = with(
        Namespace(
            groupId = "org.khorum.oss.plugins.open.publishing",
            artifactId = "digital-ocean-spaces",
            version = "1.0.0",
            publicationName = "digitalOceanSpaces"
        )
    ) {

        baseTests {
            artifactVersion eq "digital-ocean-spaces-1.0.0"
            groupPath eq "org/khorum/oss/plugins/open/publishing"
            artifactPath eq "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces"
            artifactFullPath eq "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0"
            pluginFullPath eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/" +
                "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin/1.0.0"
        }

        jarTests {
            jarName eq "digital-ocean-spaces-1.0.0.jar"
            jarSha1Name eq "digital-ocean-spaces-1.0.0.jar.sha1"
            jarSha256Name eq "digital-ocean-spaces-1.0.0.jar.sha256"
            jarPath eq "libs/digital-ocean-spaces-1.0.0.jar"
            jarSha1Path eq "libs/digital-ocean-spaces-1.0.0.jar.sha1"
            jarSha256Path eq "libs/digital-ocean-spaces-1.0.0.jar.sha256"
            jarKey eq "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0.jar"
            jarSha1Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0.jar.sha1"
            jarSha256Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0.jar.sha256"
            jarAscName eq "digital-ocean-spaces-1.0.0.jar.asc"
            jarAscPath eq "libs/digital-ocean-spaces-1.0.0.jar.asc"
            jarAscKey eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0.jar.asc"
        }

        sourcesTests {
            sourcesJarName eq "digital-ocean-spaces-1.0.0-sources.jar"
            sourcesJarSha1Name eq "digital-ocean-spaces-1.0.0-sources.jar.sha1"
            sourcesJarSha256Name eq "digital-ocean-spaces-1.0.0-sources.jar.sha256"
            sourcesJarPath eq "libs/digital-ocean-spaces-1.0.0-sources.jar"
            sourcesJarSha1Path eq "libs/digital-ocean-spaces-1.0.0-sources.jar.sha1"
            sourcesJarSha256Path eq "libs/digital-ocean-spaces-1.0.0-sources.jar.sha256"
            sourcesJarKey eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0-sources.jar"
            sourcesJarSha1Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0-sources.jar.sha1"
            sourcesJarSha256Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0-sources.jar.sha256"
            sourcesJarAscName eq "digital-ocean-spaces-1.0.0-sources.jar.asc"
            sourcesJarAscPath eq "libs/digital-ocean-spaces-1.0.0-sources.jar.asc"
            sourcesJarAscKey eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0-sources.jar.asc"
        }

        javadocTests {
            javadocJarName eq "digital-ocean-spaces-1.0.0-javadoc.jar"
            javadocJarSha1Name eq "digital-ocean-spaces-1.0.0-javadoc.jar.sha1"
            javadocJarSha256Name eq "digital-ocean-spaces-1.0.0-javadoc.jar.sha256"
            javadocJarPath eq "libs/digital-ocean-spaces-1.0.0-javadoc.jar"
            javadocJarSha1Path eq "libs/digital-ocean-spaces-1.0.0-javadoc.jar.sha1"
            javadocJarSha256Path eq "libs/digital-ocean-spaces-1.0.0-javadoc.jar.sha256"
            javadocJarKey eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0-javadoc.jar"
            javadocJarSha1Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0-javadoc.jar.sha1"
            javadocJarSha256Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0-javadoc.jar.sha256"
            javadocJarAscName eq "digital-ocean-spaces-1.0.0-javadoc.jar.asc"
            javadocJarAscPath eq "libs/digital-ocean-spaces-1.0.0-javadoc.jar.asc"
            javadocJarAscKey eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0-javadoc.jar.asc"
        }

        kdocTests {
            kdocJarName eq "digital-ocean-spaces-1.0.0-kdoc.jar"
            kdocJarSha1Name eq "digital-ocean-spaces-1.0.0-kdoc.jar.sha1"
            kdocJarSha256Name eq "digital-ocean-spaces-1.0.0-kdoc.jar.sha256"
            kdocJarPath eq "libs/digital-ocean-spaces-1.0.0-kdoc.jar"
            kdocJarSha1Path eq "libs/digital-ocean-spaces-1.0.0-kdoc.jar.sha1"
            kdocJarSha256Path eq "libs/digital-ocean-spaces-1.0.0-kdoc.jar.sha256"
            kdocJarKey eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0-kdoc.jar"
            kdocJarSha1Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0-kdoc.jar.sha1"
            kdocJarSha256Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0-kdoc.jar.sha256"
            kdocJarAscName eq "digital-ocean-spaces-1.0.0-kdoc.jar.asc"
            kdocJarAscPath eq "libs/digital-ocean-spaces-1.0.0-kdoc.jar.asc"
            kdocJarAscKey eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0-kdoc.jar.asc"
        }

        pomTests {
            sourcePomName eq "publications/digitalOceanSpaces/pom-default.xml"
            pomName eq "digital-ocean-spaces-1.0.0.pom"
            pomSha1Name eq "digital-ocean-spaces-1.0.0.pom.sha1"
            pomSha256Name eq "digital-ocean-spaces-1.0.0.pom.sha256"
            pomPath eq "libs/digital-ocean-spaces-1.0.0.pom"
            pomSha1Path eq "libs/digital-ocean-spaces-1.0.0.pom.sha1"
            pomSha256Path eq "libs/digital-ocean-spaces-1.0.0.pom.sha256"
            pomKey eq "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0.pom"
            pomSha1Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0.pom.sha1"
            pomSha256Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0.pom.sha256"
            pomAscName eq "digital-ocean-spaces-1.0.0.pom.asc"
            pomAscPath eq "libs/digital-ocean-spaces-1.0.0.pom.asc"
            pomAscKey eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/1.0.0/digital-ocean-spaces-1.0.0.pom.asc"
        }

        pluginPomTests {
            pluginJarName eq "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.jar"
            pluginJarSha1Name eq "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.jar.sha1"
            pluginJarSha256Name eq "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.jar.sha256"
            pluginJarPath eq "libs/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.jar"
            pluginJarSha1Path eq "libs/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.jar.sha1"
            pluginJarSha256Path eq "libs/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.jar.sha256"
            pluginJarKey eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/" +
                "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin/" +
                "1.0.0/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.jar"
            pluginJarSha1Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/" +
                "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin/" +
                "1.0.0/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.jar.sha1"
            pluginJarSha256Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/" +
                "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin/" +
                "1.0.0/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.jar.sha256"
            pluginJarAscName eq "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.jar.asc"
            pluginJarAscPath eq "libs/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.jar.asc"
            pluginJarAscKey eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/" +
                "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin/" +
                "1.0.0/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.jar.asc"
        }


        pluginPomTests {
            pluginPomName eq "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.pom"
            pluginPomSha1Name eq "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.pom.sha1"
            pluginPomSha256Name eq "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.pom.sha256"
            pluginPomPath eq "libs/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.pom"
            pluginPomSha1Path eq "libs/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.pom.sha1"
            pluginPomSha256Path eq "libs/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.pom.sha256"
            pluginPomKey eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/" +
                "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin/" +
                "1.0.0/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.pom"
            pluginPomSha1Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/" +
                "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin/" +
                "1.0.0/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.pom.sha1"
            pluginPomSha256Key eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/" +
                "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin/" +
                "1.0.0/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.pom.sha256"
            pluginPomAscName eq "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.pom.asc"
            pluginPomAscPath eq "libs/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.pom.asc"
            pluginPomAscKey eq
                "org/khorum/oss/plugins/open/publishing/digital-ocean-spaces/" +
                "org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin/" +
                "1.0.0/org.khorum.oss.plugins.open.publishing.digital-ocean-spaces.gradle.plugin-1.0.0.pom.asc"
        }
    }
}

fun baseTests(runnable: () -> Unit) {
    // This function can be used to run common tests or setup before running the actual test
    // For example, you can set up a mock environment or initialize resources
    runnable()
}

fun jarTests(runnable: () -> Unit) {
    // This function can be used to run tests related to JAR files
    // For example, you can check if the JAR file is created correctly or if it contains the expected classes
    runnable()
}

fun sourcesTests(runnable: () -> Unit) {
    // This function can be used to run tests related to sources JAR files
    // For example, you can check if the sources JAR file is created correctly or if it contains the expected source files
    runnable()
}

fun javadocTests(runnable: () -> Unit) {
    // This function can be used to run tests related to Javadoc JAR files
    // For example, you can check if the Javadoc JAR file is created correctly or if it contains the expected documentation
    runnable()
}

fun kdocTests(runnable: () -> Unit) {
    // This function can be used to run tests related to KDoc JAR files
    // For example, you can check if the KDoc JAR file is created correctly or if it contains the expected documentation
    runnable()
}

fun pomTests(runnable: () -> Unit) {
    // This function can be used to run tests related to POM files
    // For example, you can check if the POM file is created correctly or if it contains the expected metadata
    runnable()
}

fun pluginJarTests(runnable: () -> Unit) {
    // This function can be used to run tests related to plugin JAR files
    // For example, you can check if the plugin JAR file is created correctly or if it contains the expected plugin classes
    runnable()
}

fun pluginPomTests(runnable: () -> Unit) {
    // This function can be used to run tests related to Gradle plugin files
    // For example, you can check if the plugin file is created correctly or if it contains the expected plugin metadata
    runnable()
}