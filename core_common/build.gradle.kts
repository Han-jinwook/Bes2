// D:/Bes2/core_common/build.gradle.kts

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt) // Hilt for potential DI in utils
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bes2.core_common"
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
        // [FINAL FIX] Add debug build type to ensure consistent variant matching across all modules.
        debug {
            // This ensures that when the :app module is built in debug mode, Gradle can find a
            // matching 'debug' variant for this module, resolving the 'unspecified' dependency error.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // AndroidX & Utils
    implementation(libs.androidx.core.ktx)
    implementation(libs.timber) // Logging

    // Hilt (if providing injectable utils, otherwise can be removed)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
