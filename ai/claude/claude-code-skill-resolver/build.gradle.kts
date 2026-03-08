import org.khorum.oss.plugins.local.publishing.digitalocean.domain.uploadToDigitalOceanSpaces
import org.khorum.oss.plugins.local.publishing.mavengenerated.domain.mavenGeneratedArtifacts
import org.khorum.oss.plugins.local.secrets.getPropertyOrEnv


val claudeCodeSkillResolver: String by rootProject.extra

plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.khorum.oss.plugins.local.publishing.maven-generated-artifacts")
    id("org.khorum.oss.plugins.local.publishing.digital-ocean-spaces")
}

group = "org.khorum.oss.plugins.open.ai.claude"
version = claudeCodeSkillResolver

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "2.0.20"))
    }
}

tasks.jar {
    archiveBaseName.set("claude-code-skill-resolver")
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
        create("claudeCodeSkillResolverPlugin") {
            id = "org.khorum.oss.plugins.open.ai.claude.claude-code-skill-resolver"
            version = version.toString()
            implementationClass = "org.khorum.oss.plugins.open.ai.claude.claudecode.skillresolver.ClaudeCodeSkillResolverPlugin"
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
    name = "Claude Code Skill Resolver"
    description = """
            This plugin will download and copy skills based on url and output structure.
            Allows to choose specific skill names.
        """
    websiteUrl = "https://github.com/khorum-oss/public-plugins/tree/main/ai/claude/claude-code-skill-resolver"

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