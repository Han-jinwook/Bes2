// D:/Bes2/core_testing/build.gradle.kts

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt) // For Hilt test utilities
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bes2.core_testing"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        //解决"More than one file was found with OS independent path 'META-INF/AL2.0'"等问题
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }
}

dependencies {
    // Core AndroidX (might be needed for some test utilities)
    api(libs.androidx.core.ktx)

    // JUnit
    api(libs.junit)

    // Mockito
    api(libs.mockito.core)
    api(libs.mockito.kotlin) // Ensure this is the correct Mockito-Kotlin artifact if using one

    // KotlinX Coroutines Test
    api(libs.kotlinx.coroutines.test)

    // AndroidX Test
    api(libs.androidx.junit) // androidx.test.ext:junit
    api(libs.androidx.espresso.core)

    // Compose Testing (if providing Compose test utilities)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui.test.junit4)
    // debugImplementation for compose tooling might not be needed here unless this module itself has previews

    // Hilt Testing (provide Hilt test utilities)
    api(libs.hilt.android) // hilt-android for HiltAndroidApp etc if needed in tests using this module
    ksp(libs.hilt.compiler) // For Hilt processing in this module if it has @AndroidEntryPoint etc.
    // api(libs.hilt.android.testing) // This is the specific Hilt testing artifact - let's add it to libs.versions.toml first

    // Consider if `hilt-android-testing` should be an `api` dependency.
    // It's often used directly in the :app or feature module tests.
    // If :core_testing provides base classes that use it, then `api` is appropriate.

    // Example of a library that might be useful for testing specific things
    // api "app.cash.turbine:turbine:0.13.0" // For Flow testing, add to toml if used
}
