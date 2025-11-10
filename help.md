## Project Overview

I'm having a persistent build issue related to unresolved references in my Android Compose project. The main error is `Unresolved reference: Bes2Theme` in my `MainActivity.kt`, but I believe this is a symptom of a deeper structural problem with my Gradle module dependencies.

### Core Problem

The `:app` module depends on the `:core_ui` module, but the dependency is listed as `unspecified` during Gradle sync, indicating a variant matching failure. I suspect `:core_ui` is not correctly configured, or is missing essential source files like `Theme.kt`, which should define `Bes2Theme`. This causes a chain reaction of build failures.

Despite multiple attempts to align Kotlin, Compose, and other library versions, the fundamental issue remains. `find_files` command confirms that `Theme.kt` and `Bes2App.kt` do not exist in the project, which is the root cause.

### `MainActivity.kt` (Error Source)

This is the file where the build process fails.

```kotlin
package com.bes2.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
// The following two imports are unresolved because the :core_ui module is likely empty or misconfigured.
import com.bes2.app.ui.Bes2App 
import com.bes2.core_ui.Bes2Theme
import com.bes2.background.service.MediaDetectionService
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // The build fails here because Bes2Theme cannot be found.
            Bes2Theme {
                Bes2App(windowSizeClass = calculateWindowSizeClass(this))
            }
        }
        startMediaDetectionService()
    }

    private fun startMediaDetectionService() {
        val serviceIntent = Intent(this, MediaDetectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}
```

### Final Error Message

```
$ ./gradlew :app:compileDebugKotlin
e: file:///D:/Bes2/app/src/main/java/com/bes2/app/MainActivity.kt:14:25 Unresolved reference: Bes2Theme
```

### `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp) 
}

android {
    namespace = "com.bes2.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bes2.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core_ui"))
    implementation(project(":background"))
    implementation(project(":data"))
    implementation(project(":photos_integration"))
    implementation(project(":ml"))
    // ... other dependencies
}
```

### `core_ui/build.gradle.kts`

```kotlin
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
        debug {}
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
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    implementation(project(":core_common"))
    implementation(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material3)
    // ... other dependencies
}
```

### `gradle/libs.versions.toml`

```toml
[versions]
activity = "1.9.0"
android-gradle-plugin = "8.4.1"
kotlin = "1.9.23"
ksp = "1.9.23-1.0.19"
compose-compiler = "1.5.11"
compose-bom = "2024.05.00"
play-services-auth = "21.0.0"
// ... other versions omitted for brevity

[libraries]
// ... libraries omitted for brevity

[plugins]
// ... plugins omitted for brevity
```
