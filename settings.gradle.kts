pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Rumbo"
include(":app")

// Core modules
include(":core:model")
include(":core:common")
include(":core:designsystem")
include(":core:navigation")
include(":core:database")
include(":core:data")

// Feature modules
include(":feature:onboarding")
include(":feature:home")
include(":feature:processes")
include(":feature:tasks")
include(":feature:progress")
include(":feature:settings")
