// D:/Bes2/domain/build.gradle.kts

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

// Java Toolchain for pure Kotlin/Java modules
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Core Model
    implementation(project(":core_model"))

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Hilt/Dagger - KSP
    implementation(libs.hilt.core)
    // [FINAL FIX] Use the pure Dagger compiler for this pure Kotlin module, not the Hilt (Android) compiler.
    ksp(libs.dagger.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
}
