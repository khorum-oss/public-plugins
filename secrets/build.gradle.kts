import org.khorum.oss.plugins.local.publishing.digitalocean.domain.uploadToDigitalOceanSpaces
import org.khorum.oss.plugins.local.publishing.mavengenerated.domain.mavenGeneratedArtifacts
import org.khorum.oss.plugins.local.secrets.getPropertyOrEnv


val secretsVersion: String by rootProject.extra

plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka")
    id("org.khorum.oss.plugins.local.publishing.maven-generated-artifacts")
    id("org.khorum.oss.plugins.local.publishing.digital-ocean-spaces")
}

group = "org.khorum.oss.plugins.open"
version = secretsVersion

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

tasks.jar {
    archiveBaseName.set("secrets")
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
        create("secretsPlugin") {
            id = "org.khorum.oss.plugins.open.secrets"
            version = version.toString()
            implementationClass = "org.khorum.oss.plugins.open.secrets.SecretsLoaderPlugin"
        }
    }
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
    publicationName = "digitalOceanSpaces"  // Must match the name expected by the DO Spaces plugin
    name = "Secrets Plugin"
    description = """
            This plugin has code and tasks for secret injection.
        """
    websiteUrl = "https://github.com/khorum-oss/public-plugins/tree/main/secrets"

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