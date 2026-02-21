package org.khorum.oss.plugins.local.publishing.mavengenerated

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.khorum.oss.plugins.local.publishing.mavengenerated.service.MavenGeneratedArtifactsPublishPluginService

class MavenGeneratedArtifactsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val service = MavenGeneratedArtifactsPublishPluginService()

        service.apply(project)
    }
}