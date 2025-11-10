plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bes2.ml"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        // no view binding or data binding for library modules unless specifically needed
    }
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // ML Kit
    implementation("com.google.mlkit:face-detection:16.1.6")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.5.0")

    // Logging
    implementation(libs.timber) // Timber 추가

    // Module dependencies (if any, uncomment and add)
    // implementation(project(":core_common"))
    // implementation(project(":core_model"))
}