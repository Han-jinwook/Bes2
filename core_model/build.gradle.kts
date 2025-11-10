// D:/Bes2/core_model/build.gradle.kts

plugins {
    alias(libs.plugins.kotlin.jvm) // 순수 Kotlin 모듈
}

// Java Toolchain for pure Kotlin/Java modules
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// kotlinOptions의 jvmTarget은 이미 루트 build.gradle.kts의 allprojects에서 "17"로 설정됨.

dependencies {
    // 이 모듈은 일반적으로 다른 의존성이 거의 없거나, serialization 라이브러리 정도만 포함할 수 있습니다.
    // 예: implementation(libs.kotlinx.serialization.json) // kotlinx-serialization-json은 libs.versions.toml에 추가 필요

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
}
