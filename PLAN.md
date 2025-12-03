# Bes2 개발 계획서 (v7.2 - Stability & Sequential Pipeline)

**Date:** 2025-12-04
**Status:** Core Logic Stabilized & Pipeline Sequentialized
**Author:** Han-jinwook

---

## 📅 Development Log

### ✅ v7.2: Stability & Sequential Execution (2025-12-04)
*   **Pipeline Re-architecture (순차 실행):**
    *   **기존:** 병렬 실행 (스캔, 분석, 추억 찾기가 동시에 돌아 서로 간섭).
    *   **변경:** **[1. 분류] -> [2. 쓰레기 알림] -> [3. 정밀 분석] -> [4. 다이어트 알림] -> [5. 추억 소환]** 의 완벽한 순차 구조 확립.
    *   **이점:** 발열 제거, 작업 충돌 방지, 사용자 경험(UX) 흐름 개선.
*   **Critical Bug Fixes:**
    *   **무한 루프 제거:** `HomeViewModel`의 '자동 리필' 로직이 `REPLACE` 정책과 충돌하여 워커를 무한 재시작하던 문제 해결.
    *   **30장 제한 해제:** '쓰레기 정리' 화면에서 30장까지만 보이던 하드코딩 제한을 제거 (전체 보기 가능).
    *   **안전 장치 강화:** AI 분석 시 '군복/야간' 사진은 흐리더라도 무조건 `MEMORY`로 분류하는 안전 장치 코드 적용 (`ImageContentClassifier`).

---

## 🚀 Next Steps (Tomorrow)

### 1. 최종 검증 (순차 실행 테스트)
*   **Step 1:** 앱 실행 시 `PhotoDiscoveryWorker`가 먼저 돌고 '쓰레기 정리' 버튼이 활성화되는지 확인.
*   **Step 2:** 이어서 `PhotoAnalysisWorker`가 돌며 '다이어트' 카운팅이 올라가는지 확인.
*   **Step 3:** 모든 분석이 끝난 **맨 마지막에** '추억 소환' 버튼이 켜지는지 확인.
*   **UI:** '쓰레기 정리' 화면에 들어갔을 때, 제한 없이 모든 사진(100장 이상)이 뜨는지 확인.

### 2. 출시 준비
*   `proguard-rules.pro` 최종 점검.
*   버전 코드 업데이트 (v7.2 기준).

---

## 🏗️ Architecture Overview (Updated)

### Sequential Data Pipeline
1.  **Scanner (`PhotoDiscoveryWorker`):** 갤러리 스캔 & 1차 분류 (Trash vs Diet).
    *   *Finish:* '쓰레기 정리' 알림 발송 & 버튼 활성화.
    *   *Trigger:* `PhotoAnalysisWorker` 자동 실행.
2.  **Analyzer (`PhotoAnalysisWorker`):** 정밀 AI 분석 (눈 감음, 흔들림 등).
    *   *Update:* 1장마다 진행률 저장 -> UI 실시간 카운팅.
3.  **Clusterer (`ClusteringWorker`):** 유사 사진 그룹핑.
    *   *Finish:* '다이어트' 알림 발송 & 버튼 활성화.
4.  **Memory Recall (`HomeViewModel`):**
    *   *Trigger:* 모든 분석(`monitorAnalysisStatus`)이 완료된 순간 감지.
    *   *Action:* `loadMemoryEvent()` 호출 -> '추억 소환' 버튼 활성화.

### Key Components
*   **`ScreenshotViewModel`:** 실시간 스크린샷 + DB 쓰레기 아이템 합산 표시 (Limit 해제됨).
*   **`NotificationHelper`:** 단계별(쓰레기, 다이어트, 추억) 알림 관리.
