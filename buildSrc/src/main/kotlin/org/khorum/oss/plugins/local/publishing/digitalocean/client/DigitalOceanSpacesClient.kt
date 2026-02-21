package org.khorum.oss.plugins.local.publishing.digitalocean.client

import org.khorum.oss.plugins.local.publishing.digitalocean.domain.DigitalOceanFile
import org.khorum.oss.plugins.local.publishing.digitalocean.domain.DigitalOceanSpacesExtension
import org.gradle.api.logging.Logger

abstract class DigitalOceanSpacesClient(
    val ext: DigitalOceanSpacesExtension,
    protected val logger: Logger
) {
    abstract fun uploadFile(doFile: DigitalOceanFile)
}