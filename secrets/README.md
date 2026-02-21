# Secrets

A Gradle plugin that loads secrets from a properties file or system properties into Gradle's extra properties, making them available across the build.

## What it does

This plugin provides two mechanisms for injecting secrets into your Gradle build:

1. **File-based**: Loads key-value pairs from a properties file (e.g. `secret.properties`) into `project.ext`
2. **System properties**: Maps JVM system properties (`-D` flags) into `project.ext` under custom key names

File-based secrets take precedence -- system properties are only loaded for keys that weren't already set by the file.

The plugin also registers a `checkSecretsExist` verification task that validates the secrets file exists and that secrets were actually loaded.

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

Apply the plugin in your `build.gradle.kts`:

```kotlin
plugins {
    id("org.khorum.oss.plugins.open.secrets") version "1.0.1-SNAPSHOT"
}
```

## Configuration

```kotlin
secretsLoader {
    // Path to the secrets properties file (relative to root project)
    // Default: "secret.properties"
    secretFile = "secret.properties"

    // Map system properties into extra properties
    systemProperties {
        // addProperty(extraPropertyKey, systemPropertyName)
        addProperty("spaces.key", "DO_SPACES_API_KEY")
        addProperty("spaces.secret", "DO_SPACES_SECRET")
    }
}
```

### Secret file format

The file uses standard Java properties format:

```properties
spaces.key=your-access-key
spaces.secret=your-secret-key
db.password=hunter2
```

### Accessing loaded secrets

Once loaded, secrets are available as Gradle extra properties:

```kotlin
val apiKey: String by project.extra
// or
val apiKey = project.findProperty("spaces.key") as String?
```

The plugin also provides a `getPropertyOrEnv` extension function that checks project properties first, then falls back to environment variables:

```kotlin
import org.khorum.oss.plugins.open.secrets.getPropertyOrEnv

val key = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
```

## Usage

### Verify secrets are loaded

```shell
./gradlew checkSecretsExist
```

This task validates that:
- The secret file path is configured
- The file exists on disk
- At least one secret was loaded

The task writes a JSON result to `build/khorum-oss/tasks/check-secrets-exist/check_secrets_exist_task_output.json`.

## License

Apache License, Version 2.0
