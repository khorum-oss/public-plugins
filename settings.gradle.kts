rootProject.name = "public-plugins"

pluginManagement {
    repositories {
        maven {
            url = uri("https://open-reliquary.nyc3.digitaloceanspaces.com/plugins")
        }
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "org.khorum.oss.plugins.local.publishing.maven-generated-artifacts") {
                useModule("org.khorum.oss.plugins.local.publishing:maven-generated-artifacts:${requested.version}")
            }
        }
    }
}

include(
    "ai:claude:claude-code-skill-resolver",
    "pipeline",
    "publishing:digital-ocean-spaces",
    "publishing:maven-generated-artifacts",
    "secrets",
    "test-core"
)