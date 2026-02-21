plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka") version "1.9.20"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

repositories {
    // Add any required repositories
    mavenCentral()
}

dependencies {
    implementation("software.amazon.awssdk:s3:2.25.27")
    implementation("org.apache.commons:commons-lang3:3.18.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.13.8") // For mocking in Kotlin
    testImplementation(gradleTestKit()) // For Gradle project/test DSLs
}

tasks.withType<Test> {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("secretsLoaderPlugin") {
            id = "org.khorum.oss.plugins.local.secrets.loader"
            version = "1.0.0"
            implementationClass = "org.khorum.oss.plugins.local.secrets.SecretsLoaderPlugin"
        }
    }

    plugins {
        create("localProjectSyncPlugin") {
            id = "org.khorum.oss.plugins.local.publishing.project-sync"
            version = "1.0.0"
            implementationClass = "org.khorum.oss.plugins.local.publishing.projectsync.ProjectSyncPlugin"
        }
    }

    plugins {
        create("localMavenGeneratedArtifactsPlugin") {
            id = "org.khorum.oss.plugins.local.publishing.maven-generated-artifacts"
            version = "1.0.0"
            implementationClass = "org.khorum.oss.plugins.local.publishing.mavengenerated.MavenGeneratedArtifactsPlugin"
        }
    }

    plugins {
        create("localDigitalOceanSpacesPlugin") {
            id = "org.khorum.oss.plugins.local.publishing.digital-ocean-spaces"
            version = "1.0.0"
            implementationClass = "org.khorum.oss.plugins.local.publishing.digitalocean.DigitalOceanSpacesPublishPlugin"
        }
    }

    plugins {
        create("jarExplorerPlugin") {
            id = "org.khorum.oss.plugins.local.jar-explorer"
            version = "1.0.0"
            implementationClass = "org.khorum.oss.plugins.local.jar.explorer.JarExplorerPlugin"
        }
    }
}