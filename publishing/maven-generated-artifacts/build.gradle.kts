import org.khorum.oss.plugins.local.publishing.digitalocean.domain.uploadToDigitalOceanSpaces
import org.khorum.oss.plugins.local.publishing.mavengenerated.domain.mavenGeneratedArtifacts
import org.khorum.oss.plugins.local.secrets.getPropertyOrEnv

val publishingMavenGeneratedArtifactsVersion: String by rootProject.extra

plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.khorum.oss.plugins.local.publishing.project-sync")
    id("org.khorum.oss.plugins.local.publishing.maven-generated-artifacts")
    id("org.khorum.oss.plugins.local.publishing.digital-ocean-spaces")
}

group = "org.khorum.oss.plugins.open.publishing"
version = publishingMavenGeneratedArtifactsVersion

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

tasks.jar {
    archiveBaseName.set("maven-generated-artifacts")
}

repositories {
    // Add any required repositories
    mavenCentral()
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation(project(":test-core"))
}

gradlePlugin {
    plugins {
        create("mavenGeneratedArtifactsPlugin") {
            id = "org.khorum.oss.plugins.open.publishing.maven-generated-artifacts"
            version = version.toString()
            implementationClass = "org.khorum.oss.plugins.open.publishing.mavengenerated.MavenGeneratedArtifactsPlugin"
        }
    }
}

projectSync {
    autoSync()
    val projectFile = rootProject.layout
        .projectDirectory
        .asFile
    syncSource = projectFile
        .resolve("publishing/maven-generated-artifacts/src/main/kotlin/org/khorum/oss/plugins/open/publishing/mavengenerated")
    syncTarget = projectFile
        .resolve("buildSrc/src/main/kotlin/org/khorum/oss/plugins/local/publishing/mavengenerated")
}

digitalOceanSpacesPublishing {
    bucket = "open-reliquary"
    accessKey = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
    secretKey = project.getPropertyOrEnv("spaces.secret", "DO_SPACES_SECRET")
    publishedVersion = version.toString()
    isPlugin = true
    dryRun = false
}

tasks.uploadToDigitalOceanSpaces?.apply {
    dependsOn(tasks.mavenGeneratedArtifacts)
}

mavenGeneratedArtifacts {
    publicationName = "digitalOceanSpaces"
    name = "Maven Generated Artifacts"
    description = """
            This plugin generates Maven artifacts such as sources, Javadoc, and KDoc JARs.
            It is used to publish these artifacts to a Maven repository or a digital ocean space.
        """
    websiteUrl = "https://github.com/khorum-oss/public-plugins/tree/main/publishing/maven-generated-artifacts"

    licenses {
        license {
            name = "Apache License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        }
    }

    developers {
        developer {
            id = "khorum-oss"
            name = "Khorum OSS Team"
            email = "khorum.oss@gmail.com"
            organization = "Khorum OSS"
        }
    }

    scm {
        connection = "https://github.com/khorum-oss/public-plugins.git"
    }
}