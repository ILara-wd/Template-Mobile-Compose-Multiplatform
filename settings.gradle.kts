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

rootProject.name = "SportKids"
include(":app")
include(":core:designsystem")
include(":core:model")
include(":core:ui")
include(":feature:auth")
include(":feature:athlete")
include(":feature:agenda")
include(":feature:home")
include(":feature:tkd")
include(":feature:swimming")
include(":feature:athletics")
 