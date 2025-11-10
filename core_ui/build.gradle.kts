// D:/Bes2/core_ui/build.gradle.kts

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bes2.core_ui"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
        // FIX: Explicitly define the NAMESPACE field for BuildConfig
        buildConfigField("String", "NAMESPACE", "\"${namespace}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        // Add debug build type to match the app module
        debug {
            // No specific settings needed, but its existence is crucial for variant matching
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
        compose = true
        buildConfig = true
    }
    composeOptions {
        // [FIX] Correct the reference to match 'compose-compiler' in libs.versions.toml
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core_common"))
    // Core AndroidX
    implementation(libs.androidx.core.ktx)

    // Compose - using api for re-exporting common UI components and themes
    implementation(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material3)

    // Hilt (for ViewModel injection in shared Composables, etc.)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Image Loading (Coil for Compose) - api if you expose CoilImage or similar Composables
    api(libs.coil.compose)

    // Logging
    implementation(libs.timber)
    
    // Potentially :core_model if UI elements directly use them, though often via ViewModel
    // implementation(project(":core_model"))

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling) // For preview in IDE and inspection
}
