// D:/Bes2/background/build.gradle.kts

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bes2.background"
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
        debug {
            // 빌드 Variant 일치를 위해 debug 유형을 명시적으로 추가
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
    implementation(project(":core_common"))
    implementation(project(":core_ui"))
    implementation(project(":core_model"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":ml"))
    
    // photos_integration is just a library, not an annotation processor.
    implementation(project(":photos_integration"))

    // Logging
    implementation(libs.timber)

    // AndroidX & Coroutines
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Hilt (for Worker injection)
    implementation(libs.hilt.android)
    implementation(libs.hilt.work)

    // KSP should only contain the annotation processors (compilers).
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
