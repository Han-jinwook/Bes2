plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp) // kapt -> ksp
}

android {
    namespace = "com.bes2.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bes2.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "1.5"

        testInstrumentationRunner = "com.bes2.app.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        missingDimensionStrategy("default", "default")
    }

    buildTypes {
        debug {
            matchingFallbacks += listOf("release")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
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
        buildConfig = true // [FIX] BuildConfig 사용 활성화
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // [FINAL FIX] Proactively exclude all common duplicate META-INF files.
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    // Core & System
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work.runtime.ktx)

    // Hilt - KSP로 통일
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.work)
    ksp(libs.androidx.hilt.compiler) // Hilt-Work 통합, kapt -> ksp
    implementation(libs.androidx.hilt.navigation.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation("androidx.compose.material3:material3-window-size-class")

    // [FINAL FIX] Use the correct alias 'androidx.material' defined in libs.versions.toml
    implementation(libs.androidx.material)
    implementation(libs.google.auth)
    implementation(libs.google.fido)
    implementation(libs.play.services.ads)
    
    // Naver Login
    implementation("com.navercorp.nid:oauth:5.9.0")

    // Coroutines
    implementation(libs.kotlinx.coroutines.play.services)

    // Other
    implementation(libs.coil.compose)
    implementation(libs.timber)

    // Project Modules
    implementation(project(":domain"))
    implementation(project(":core_ui"))
    implementation(project(":background"))
    implementation(project(":data"))
    implementation(project(":photos_integration"))
    implementation(project(":ml"))

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(project(":core_testing"))
    androidTestImplementation(project(":core_testing"))
}
