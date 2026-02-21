package org.khorum.oss.plugins.open.pipeline.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Detects which Gradle modules have changed files relative to origin/main.
 *
 * Changed files are determined by either:
 * 1. The `CHANGED_FILES` environment variable (space-separated file paths), or
 * 2. Running `git diff --name-only origin/main...HEAD`
 *
 * Output is printed as `MATRIX=[...]` JSON, designed for consumption by
 * GitHub Actions matrix strategies. Each entry contains:
 * - `module`: Gradle path (e.g. `:publishing:digital-ocean-spaces`)
 * - `path`: Filesystem path (e.g. `publishing/digital-ocean-spaces`)
 * - `filename`: Hyphenated form for use as artifact names (e.g. `publishing-digital-ocean-spaces`)
 */
open class DetectChangeModulesTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @TaskAction
    fun detectChangeModules() {
        println("Detecting changed modules...")
        val changedFiles = getChangedFiles()

        if (changedFiles.isEmpty()) {
            println("""MATRIX=[]""")
            return
        }

        val gradleModules = project.subprojects.map { it.path }
        val allModulePaths = gradleModules.toPaths()
        val changedFileModulePaths = allModulePaths.filterHasChangedFiles(changedFiles)
        val moduleDetails = changedFileModulePaths.toNamespaceJson()

        println("MATRIX=$moduleDetails")
    }

    /**
     * Gets changed files from the CHANGED_FILES env var (CI-provided),
     * falling back to a live git diff against origin/main.
     */
    private fun getChangedFiles(): List<String> {
        return System.getenv("CHANGED_FILES")
            ?.split(" ")
            ?.filter { it.isNotBlank() }
            ?: run { execOperations.getDiff() }
    }

    private fun ExecOperations.getDiff(): List<String> {
        val outputStream = ByteArrayOutputStream()
        this.exec {
            commandLine("git", "diff", "--name-only", "origin/main...HEAD")
            standardOutput = outputStream
        }
        return outputStream.toFilePathList()
    }

    private fun ByteArrayOutputStream.toFilePathList(): List<String> {
        return toString()
            .trim()
            .split("\n")
            .filter { it.isNotBlank() }
    }

    /** Converts Gradle `:` paths (e.g. `:a:b`) to filesystem paths (e.g. `a/b`). */
    private fun List<String>.toPaths(): Set<String> {
        return map { it.removePrefix(":").replace(":", "/") }
            .sortedByDescending { it.length }
            .toSet()
    }

    /**
     * Matches each changed file to the module it belongs to.
     * Files under `src/` at the root are included as the special "src" module.
     * Sorted longest-path-first so nested modules match before their parents.
     */
    private fun Set<String>.filterHasChangedFiles(changedFiles: List<String>): Set<String> {
        return changedFiles.mapNotNull { file ->
            this.find { module ->
                file.startsWith("$module/") || file.startsWith("$module\\")
            } ?: "src".takeIf { file.startsWith(it) }
        }.toSet()
    }

    /** Formats matched modules as a JSON array for GitHub Actions matrix consumption. */
    private fun Set<String>.toNamespaceJson(): String {
        return joinToString(",", "[", "]") {
            val module = it.replace("/", ":")
            val filename = it.replace("/", "-")

            """{"module":":$module","path":"$it","filename":"$filename"}"""
        }
    }
}