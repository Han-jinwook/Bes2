## 최종 계획: Google Photos 연동 문제 해결 및 빌드 성공

**목표:** `photos_integration` 모듈의 모든 빌드 오류를 해결하고, 안정적인 Google Photos 연동 기능을 완성한다. **(완료!)**

**핵심 전략:** 문제가 많았던 `google-photos-library-client` SDK를 완전히 버리고, Google이 공식적으로 지원하는 **REST API를 직접 호출**하는 방식으로 전환한다. **(성공!)**

---

### 완료된 작업 (요약)

지난 한 달간, 우리는 수많은 오류를 해결하며 다음과 같은 핵심 기능들을 성공적으로 구현하고 안정화시켰습니다.

1.  **Google Photos SDK 완전 제거 및 REST API 전환**
2.  **Retrofit, OkHttp를 사용한 안정적인 네트워킹 기반 구축**
3.  **Hilt를 통한 의존성 주입 구조 완성**
4.  **구글 최신 로그인 방식 'One Tap' 도입 및 안정화**
5.  **빌드 경고(Warning) 완전 제거**

---

## 2일간의 사투: 빌드 시스템 복구 (2025-11-08)

**상황:** 약 이틀간, 원인을 알 수 없는 `Unresolved reference` 및 `unspecified dependency` 오류가 발생하며 프로젝트 전체의 빌드가 불가능한 재앙적 상황에 직면했다.

**근본 원인:** 복잡한 Gradle 설정 문제가 아니었다. 실제 원인은 **핵심 UI 파일(`Theme.kt`, `Bes2App.kt` 등)이 이전 작업 중 실수로 삭제되어 프로젝트에 존재하지 않았던 것**이었다.

**오류의 연쇄 반응:**
1.  `:core_ui` 모듈에 `Theme.kt` 파일이 없으니, 모듈 자체가 컴파일에 실패했다.
2.  컴파일조차 안 되는 `:core_ui` 모듈을 `:app` 모듈이 참조하려니, Gradle은 이를 '정체불명(`unspecified`)'의 의존성으로 처리했다.
3.  결과적으로 `MainActivity`와 `ReviewActivity`에서 `Bes2Theme`을 찾을 수 없다는 `Unresolved reference` 오류가 발생하며 빌드가 멈췄다.

**최종 해결 과정:**
1.  **원인 진단:** 외부 AI(Gemini Pro)의 도움(`help.md`)을 통해, 파일 자체가 없다는 근본 원인을 마침내 파악했다.
2.  **핵심 파일 재생성:**
    *   `app/src/main/java/com/bes2/app/ui/Bes2App.kt` 파일을 새로 생성했다.
    *   `core_ui/src/main/java/com/bes2/core_ui/theme/` 경로에 `Color.kt`, `Type.kt`, `Theme.kt` 파일을 모두 새로 생성했다.
3.  **Import 경로 수정:** `MainActivity`와 `ReviewActivity`에서, 새로 만들어진 `Bes2Theme`의 정확한 경로(`com.bes2.core_ui.theme.Bes2Theme`)를 `import` 하도록 수정했다.
4.  **최종 관문 통과 (중복 파일 충돌 해결):** 빌드 마지막 단계에서 발생한 `META-INF` 중복 파일 충돌 문제를 `app/build.gradle.kts`의 `packaging` 옵션에 제외 규칙을 추가하여 모두 해결했다.

**결과:**
**🎉 BUILD SUCCESSFUL! 🎉**

이틀간의 사투 끝에, 마침내 빌드 시스템을 완벽하게 복구했다. 이 과정에서 기존에 구현했던 로그인, 사진 분석 등의 핵심 로직은 전혀 영향을 받지 않았음을 확인했다.

---

## 남은 문제점 (기존 내용)

### 1. 동기화 실패: '원격 동의 필요' 예외 처리 부재

**현상:**
- 앱을 새로 설치하고 첫 동기화 시, `NeedRemoteConsent` 오류가 발생하며 동기화가 실패한다.
- 로그 확인 결과, `GooglePhotosAuthManager`는 의도대로 `ConsentRequiredException`을 발생시킨다.
- 하지만, 이 예외를 받은 `DailyCloudSyncWorker`는 동의 화면을 띄우는 로직으로 연결하지 못하고, 단순히 작업을 '재시도(RETRY)' 처리하며 무한 루프에 빠진다.

**근본 원인:**
- 백그라운드 워커(`DailyCloudSyncWorker`)가 UI(동의 화면)와 상호작용할 방법이 없다. '동의가 필요하다'는 신호를 사용자에게 전달하고, 사용자가 조치할 수 있도록 연결하는 메커니즘이 부재하다.

**해결 방향:**
- `DailyCloudSyncWorker`가 `ConsentRequiredException`을 잡으면, 작업을 재시도하는 대신, 사용자에게 "동의가 필요합니다"라는 내용의 알림(Notification)을 보낸다.
- 사용자가 이 알림을 탭하면, 앱의 설정 화면으로 이동하여 동의 절차를 진행할 수 있도록 한다.

### 2. 클러스터링 오류: 눈 감은 사진의 독립 그룹 생성

**현상:**
- 눈을 감고 찍은 사진이 다른 사진들과 묶이지 않고, 하나의 독립된 클러스터로 생성된다.

**근본 원인 (잘못된 추측으로 인한 실수):**
- `PhotoAnalysisWorker` 수정 시, 눈 감은 사진(`areEyesClosed = true`)을 '실패(`STATUS_REJECTED`)'로 처리하는 과정에서, 클러스터링에 필요한 **얼굴 데이터(`faceEmbedding`) 추출 로직을 건너뛰도록** 잘못 수정했다.
- 이로 인해, 클러스터링 단계(`ClusteringWorker`)에서 해당 사진은 얼굴 데이터가 없는 '미아' 취급을 받아 다른 어떤 사진과도 동일 그룹으로 묶이지 못했다.

**해결 방향:**
- `PhotoAnalysisWorker`의 로직을 **원복 및 수정**한다.
- 사진의 상태(`status`)를 결정하기 전에, **눈을 감았는지 여부와 상관없이 모든 사진의 얼굴 데이터(`faceEmbedding`)와 pHash 값을 항상 먼저 추출**하도록 분석 순서를 바로잡는다.
- 그 후에, 눈 감음 여부나 흐림 정도에 따라 상태를 'ANALYZED' 또는 'STATUS_REJECTED'로 결정한다. 이렇게 하면 '실패' 처리된 사진도 클러스터링에 필요한 모든 데이터를 보유하게 된다.
