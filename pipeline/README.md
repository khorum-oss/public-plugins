# Pipeline

A Gradle plugin that provides CI/CD tasks for detecting changed modules and listing project modules, designed for use with GitHub Actions.

## What it does

This plugin registers two tasks:

- **`printModules`** -- Prints all subproject module paths (one per line), useful for dynamically generating CI job matrices
- **`detectChangedModules`** -- Determines which modules have changed relative to `origin/main` and outputs a JSON array for GitHub Actions matrix strategies

## Installation

Add the plugin repository to your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://open-reliquary.nyc3.digitaloceanspaces.com")
        }
        gradlePluginPortal()
    }
}
```

Apply the plugin in your root `build.gradle.kts`:

```kotlin
plugins {
    id("org.khorum.oss.plugins.open.pipeline") version "1.0.1-SNAPSHOT"
}
```

## Configuration

```kotlin
pipeline {
    // Whether to include "src" as a top-level module in printModules output
    // Default: false
    hasSrc = false
}
```

## Usage

### Print all modules

```shell
./gradlew printModules
```

Outputs each subproject path on its own line:

```
publishing/digital-ocean-spaces
publishing/maven-generated-artifacts
ai/claude/claude-code-skill-resolver
pipeline
secrets
```

### Detect changed modules

```shell
./gradlew detectChangedModules
```

Outputs a `MATRIX=` line with a JSON array of changed modules:

```
MATRIX=[{"module":":pipeline","path":"pipeline","filename":"pipeline"},{"module":":secrets","path":"secrets","filename":"secrets"}]
```

Each entry contains:
- `module` -- Gradle project path (e.g. `:publishing:digital-ocean-spaces`)
- `path` -- Filesystem path (e.g. `publishing/digital-ocean-spaces`)
- `filename` -- Hyphenated form suitable for artifact names (e.g. `publishing-digital-ocean-spaces`)

#### How change detection works

Changed files are determined by one of two sources:

1. **`CHANGED_FILES` environment variable** -- If set, the task uses this space-separated list of file paths (useful when your CI already computes the diff)
2. **`git diff`** -- Otherwise, runs `git diff --name-only origin/main...HEAD` to find files changed on the current branch

Each changed file is then matched to its owning Gradle module. Files under `src/` at the root level are reported as a special `"src"` module.

#### GitHub Actions example

```yaml
jobs:
  detect:
    runs-on: ubuntu-latest
    outputs:
      matrix: ${{ steps.detect.outputs.matrix }}
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      - id: detect
        run: |
          OUTPUT=$(./gradlew -q detectChangedModules 2>/dev/null | grep "^MATRIX=")
          echo "matrix=${OUTPUT#MATRIX=}" >> "$GITHUB_OUTPUT"

  build:
    needs: detect
    if: needs.detect.outputs.matrix != '[]'
    strategy:
      matrix:
        module: ${{ fromJson(needs.detect.outputs.matrix) }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: ./gradlew ${{ matrix.module.module }}:build
```

## License

Apache License, Version 2.0