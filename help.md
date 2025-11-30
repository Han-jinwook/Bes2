# Bes2 긴급 디버깅 및 해결 과제 (Session Transfer)

**Date:** 2025-11-30 23:45
**Status:** Critical Fixes Applied, Verification Needed

---

## 1. 현재 해결되지 않은 문제 (Known Issues)

### ① 감지 서비스 알림 (Foreground Notification) 미표시
*   **현상:** 앱 실행 시 상단바에 "사진을 감지하고 있습니다" 알림이 뜨지 않음.
*   **원인:** `InvalidForegroundServiceTypeException` (Android 14+) 크래시를 막기 위해 `PhotoDiscoveryWorker` 등에서 `setForeground()` 호출을 임시로 제거함.
*   **해결 과제:** `AndroidManifest.xml`의 `foregroundServiceType="dataSync"` 설정이 병합 과정에서 누락되지 않도록 확실히 조치한 후, `setForeground()`를 다시 복구해야 함.

### ② 다이어트(Diet) & 쓰레기(Trash) 기능 먹통
*   **현상:** `PhotoDiscoveryWorker` 로그에 `No more images to scan`이 뜨며 아무 사진도 가져오지 못함.
*   **원인:** `GalleryRepository.getRecentImages()`의 쿼리 조건(`BUCKET_DISPLAY_NAME NOT LIKE %Screenshot%`)이 예상보다 많은 사진을 필터링했을 가능성.
*   **조치 완료:** 쿼리 조건(`selection`)을 제거하고 모든 사진을 가져오도록 수정함. (다음 빌드에서 검증 필요)

### ③ 리뷰 화면 삭제 후 멈춤 (Freezing)
*   **현상:** 마지막 사진 묶음을 정리(삭제/저장)한 후, 화면이 넘어가지 않고 멈추거나 빈 화면이 됨.
*   **원인:** `ReviewViewModel.nextCluster()`에서 마지막 인덱스(`currentIndex == total - 1`)일 때 종료 로직(`finishReview`)이 누락됨.
*   **조치 완료:** `nextCluster()`에 종료 로직 추가함. (다음 빌드에서 검증 필요)

---

## 2. 최근 수정 사항 (Recent Fixes)

### 🔹 4번 기능 (Instant Review) 로직 확정
*   **기준점:** `MediaDetectionService` 시작 시간(`APP_START_TIME`) 이후에 촬영된 사진만 스캔.
*   **동작:** `PhotoDiscoveryWorker`가 `INSTANT` 모드일 때, 위 기준점 이후의 사진만 `source_type='INSTANT'`로 저장.
*   **결과:** "분류된 정리하기" 버튼 클릭 시, 엉뚱한 과거 사진이 섞이지 않고 **방금 찍은 사진들만** 깔끔하게 보임.

### 🔹 Hilt 의존성 및 초기화 문제 해결
*   **`Bes2Application`:** `WorkManager` 초기화 충돌(`IllegalStateException`) 해결. `onCreate` 내 동기 작업(`enqueueUniquePeriodicWork`)을 백그라운드 스레드로 이동하여 ANR 해결.
*   **`DatabaseModule`:** 가짜 `ImageItemDao` 제거 및 정상 DAO 연결.

---

## 3. 다음 세션 목표 (Next Steps)

1.  **빌드 및 기능 검증:**
    *   `GalleryRepository` 쿼리 수정 후 다이어트/쓰레기 숫자가 올라가는지 확인.
    *   리뷰 화면에서 마지막 묶음 처리 후 홈으로 잘 돌아오는지 확인.
2.  **Foreground Service 복구:**
    *   `app` 모듈 매니페스트 설정을 재점검하고, `PhotoDiscoveryWorker`에 `setForeground`를 다시 적용하여 **"감지 서비스 알림"**을 되살릴 것. (안드로이드 정책 준수)
3.  **UI 디테일:** 
    *   눈 감은 사진이 베스트로 선정되는 등의 AI 점수 로직 튜닝.
