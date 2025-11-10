pluginManagement {
    repositories {
        google()
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

rootProject.name = "Bes2"
include(":app")
include(":data")
include(":domain")
include(":core_common")
include(":core_model")
include(":core_ui")
include(":core_testing")
include(":background")
include(":ml")
include(":photos_integration")

// Force project re-evaluation
