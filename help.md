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

---

### 새로운 난제 (세션 2) - 리뷰 완료 후 사진 상태(`KEPT`) 불일치 문제

#### 1. 최종 목표
사진 리뷰 화면(`ReviewScreen`)에서 베스트 사진 2장을 남기고 나머지 사진을 삭제하면, 남겨진 사진들의 DB 상태가 **"KEPT"**로 변경되어, 자동/수동 동기화 시점에 클라우드로 업로드되어야 한다.

#### 2. 현재 현상 (요약)
*   **결론:** 리뷰 완료 후, 보관된 사진의 상태가 DB에서 "KEPT"로 변경되지 않아, 동기화 작업(`DailyCloudSyncWorker`)이 업로드할 사진을 찾지 못하고 있다.
*   **증상 1:** 리뷰 완료 직후 실행되는 자동 동기화("분석 직후 바로바로 동기화") 시 알림이 오지 않음.
*   **증상 2:** 설정 화면에서 "지금 바로 동기화" 버튼을 누르면, "백업할 새로운 베스트 사진이 없습니다." 라는 토스트 메시지가 즉시 나타남.
*   **로그 확인:** `DailyCloudSyncWorker` 로그에 `No new images to upload.` 라고 기록되며, 이는 데이터베이스 쿼리 `getImagesByStatusAndUploadFlag("KEPT", false)`의 결과가 0개임을 의미함.

#### 3. 현재까지의 진행 상황 및 코드 상태
*   **프로젝트 상태:** 모든 코드는 안정적인 마지막 커밋(`9930df1a`) 상태에서 시작하여, 아래 문제들을 해결한 상태임.
*   **Google 로그인 문제 해결:** Android Studio 재설치로 인해 변경된 `debug.keystore`의 SHA-1 지문을 Google Cloud Console에 새로 등록하여 해결 완료.
*   **Naver/Google 인증 흐름 최종 수정:** 백그라운드에서는 로그인 `Intent`를 직접 만들지 않고, `ConsentRequiredException`만 발생시키도록 로직을 정리함. (현재 인증 흐름은 정상)
*   **`ReviewViewModel.kt` 상태:** "삭제" 버튼을 눌러 리뷰를 완료하는 시나리오에서, 선택된 베스트 사진들의 상태를 `"KEPT"`로 변경하는 로직이 `deleteOtherImages()` 및 `onDeletionRequestHandled()` 함수 내에 **분명히 존재함.**

#### 4. 핵심 미스터리 및 다음 세션을 위한 제안
*   **핵심 문제:** `ReviewViewModel`의 코드는 `imageItemDao.updateImageStatusesByIds(keptImageIds, "KEPT")`를 분명히 호출하고 있음에도 불구하고, 실제 DB 상태는 변경되지 않는 것으로 보인다.
*   **가능성 1 (가장 유력):** `ReviewViewModel`의 `init` 블록에서 관찰하는 `Flow`의 동작 방식 때문에, DB 업데이트 트랜잭션이 완료되기 **전에** 다음 클러스터 상태(`No clusters to review`)가 방출되는 **경쟁 상태(Race Condition)**가 발생하고 있다. 로그를 보면, DB 업데이트가 포함된 `onDeletionRequestHandled` 함수가 끝나기도 전에, `No clusters to review. Triggering post-review sync` 로그가 먼저 찍히는 것을 알 수 있다.
*   **다음 세션 제안:**
    1.  `ReviewViewModel`의 `onDeletionRequestHandled` 함수 로직을 `suspend fun`으로 변경하고, `imageClusterDao.updateImageClusterReviewStatus`까지의 모든 DB 작업이 완료될 때까지 기다리도록 `viewModelScope.launch` 블록을 수정해야 함.
    2.  리뷰 완료 후 다음 상태로 넘어가는 현재의 `Flow` 기반 구조가 너무 복잡하여 경쟁 상태를 유발하고 있으므로, `StateFlow`를 직접 업데이트하는 등, 상태 관리 방식을 조금 더 단순하고 명시적으로 변경하는 것을 고려해야 함.
