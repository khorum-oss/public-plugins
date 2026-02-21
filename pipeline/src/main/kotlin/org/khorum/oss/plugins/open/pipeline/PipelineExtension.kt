package org.khorum.oss.plugins.open.pipeline

/**
 * Configuration for the Pipeline plugin.
 *
 * @property hasSrc When true, the `printModules` task includes "src" as a
 *   top-level module in its output. This is useful for projects that have
 *   source code at the root level outside of any Gradle subproject.
 */
open class PipelineExtension {
    var hasSrc: Boolean = false
}