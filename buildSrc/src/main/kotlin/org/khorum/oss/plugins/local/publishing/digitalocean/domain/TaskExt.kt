package org.khorum.oss.plugins.local.publishing.digitalocean.domain

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

val TaskContainer.uploadToDigitalOceanSpaces: Task?
    get() = this.findByName("uploadToDigitalOceanSpaces")