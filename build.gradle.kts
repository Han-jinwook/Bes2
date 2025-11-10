// D:/Bes2/build.gradle.kts
plugins {
    // [FINAL CLEANUP] All plugin versions are now centrally managed by `gradle/libs.versions.toml`.
    // This root build script simply declares which plugin aliases are available to sub-projects.
    // `apply false` ensures they are not applied to the root project itself.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.androidx.room) apply false
}

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
