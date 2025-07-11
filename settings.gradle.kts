
pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.2.0"
        id("com.android.application") version "8.11.1"
        id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    }
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

rootProject.name = "WalkAir"
include(":app")
include(":wear")
