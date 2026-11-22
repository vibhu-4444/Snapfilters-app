pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Snapfilters"

// App
include(":app")

// Core layer
include(":core:common")
include(":core:designsystem")
include(":core:database")
include(":core:network")

// Feature layer (10 modules, strictly isolated from each other per ADR-001)
include(":feature:camera")
include(":feature:filters")
include(":feature:ar")
include(":feature:editor")
include(":feature:gallery")
include(":feature:sharing")
include(":feature:auth")
include(":feature:groups")
include(":feature:chat")
include(":feature:settings")

