### 주제: 다중 모듈 Hilt 주입 시 발생한 `Variant Ambiguity` 빌드 오류 해결 과정 요약

#### 1. 최종 목표
`:background` 모듈이 `:photos_integration` 모듈에 정의된 `NaverMyBoxProvider`를 Hilt로 주입받는 것.

#### 2. 핵심 문제 및 잘못된 접근
*   **오류:** `Cannot choose between the available variants of project :photos_integration`
*   **진짜 원인:** `:background` 모듈의 `build.gradle.kts`에서, **라이브러리 모듈**인 `:photos_integration`을 **Annotation Processor를 위한** `ksp` 설정에 잘못 추가함.
*   **잘못된 시도들:** `publishing` 블록 추가, `kspDebug`/`kspRelease` 분리, `attributes` 강제 주입 등. 이들은 모두 잘못된 `ksp` 설정을 전제로 했기에 근본적인 해결책이 될 수 없었음.

#### 3. 최종 해결책 (2단계)
1.  **Gradle 문제 해결 (근본 원인 제거):**
    *   **:background/build.gradle.kts** 에서 `ksp(project(":photos_integration"))` 와 그 변형(`kspDebug` 등)을 **완전히 삭제**함.
    *   `:photos_integration`은 `implementation(project(":photos_integration"))` 으로만 의존성을 유지함. `ksp` 설정에는 오직 Hilt 컴파일러 라이브러리만 남김.

2.  **연쇄적 컴파일 오류 해결:**
    *   위 Gradle 문제 해결 후, `Unresolved reference` 컴파일 오류가 발생함.
    *   **:data** 모듈의 `StoredSettings` 데이터 클래스의 필드명을 (`provider` -> `cloudStorageProvider`) 수정함.
    *   해당 데이터 클래스를 사용하던 **:background** 모듈(`DailyCloudSyncWorker`)과 **:app** 모듈(`SettingsViewModel`)의 코드도 변경된 필드명에 맞게 모두 수정하여 해결함.
