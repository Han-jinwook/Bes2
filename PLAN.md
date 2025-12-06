# Bes2 개발 계획서 (v7.4 - Final Pipeline Stabilization)

**Date:** 2025-12-06
**Status:** Pipeline Fully Stabilized & Core Bugs Fixed
**Author:** Han-jinwook

---

## 📅 Development Log

### ✅ v7.4: Final Stabilization (2025-12-06)
*   **Pipeline Logic Finalized (5단계 순차 실행 완성):**
    *   **엄격한 순서 보장:** [1. 분류] -> [2. 쓰레기 알림] -> [3. 정밀 분석] -> [4. 클러스터링/다이어트 알림] -> **[5. 추억 소환]**
    *   **안전장치 강화:** 5단계(추억 소환)는 반드시 **4단계(`ClusteringWorker`)가 성공(`SUCCEEDED`)한 것을 확인한 후**에만 실행되도록 `HomeViewModel` 로직 수정. (앱 시작 시 오작동 및 중간 실행 방지)
*   **Critical Bug Fixes:**
    *   **사물 사진 미분류 해결:** AI가 애매하다고 판단한 사진(`Uncertain`)을 기존 `MEMORY`에서 **`OBJECT` (쓰레기)**로 분류하도록 정책 변경. (`ImageContentClassifier`) -> 책상, 의자 등 사물 사진이 정상적으로 쓰레기통으로 이동.
    *   **쓰레기 목록 30장 제한 해제:** `TrashItemDao`에서 쿼리 `LIMIT` 제거 및 `ScreenshotViewModel` 로직 수정으로 모든 쓰레기 사진 표시.
    *   **스캔 중단 문제 해결:** `PhotoDiscoveryWorker`에서 손상된 이미지 파일 처리 시 전체 스캔이 멈추지 않도록 `try-catch` 범위를 개별 파일 단위로 적용.

### ✅ v7.3: Notification Fixes & Scan Optimization (2025-12-04)
*   **Notification Overwrite Fix:** 알림 ID 분리 (`TRASH`: 1001, `DIET`: 1002, `MEMORY`: 1003).
*   **Scan Logic Refinement:** 앱 최초 실행 시 즉시 스캔 트리거.

---

## 🏗️ Architecture Overview (Final)

### 5-Step Sequential Data Pipeline
1.  **Scanner (`PhotoDiscoveryWorker`):** 갤러리 스캔 & 1차 분류.
    *   *AI Policy:* 확실한 사물/문서 -> Trash. 애매한 것 -> **Trash (Object)**. 확실한 인물/음식 -> Diet.
2.  **Trash Notification:** 스캔 완료 직후 쓰레기 알림 발송.
3.  **Analyzer (`PhotoAnalysisWorker`):** Diet 대상 사진 정밀 분석 (흔들림, 눈 감음 등).
4.  **Clusterer (`ClusteringWorker`):** 유사 사진 그룹핑 & 다이어트 알림 발송.
    *   *Finish Condition:* 이 단계가 `SUCCEEDED` 상태가 되어야만 다음 단계로 넘어감.
5.  **Memory Recall (`HomeViewModel` -> `MemoryEventWorker`):**
    *   *Trigger:* **4단계(Clusterer) 완료가 확인되면** `startMemoryAnalysis` 호출.
    *   *Action:* 하루 20장 이상 촬영된 날짜 분석 -> '추억 소환' 버튼 활성화 & 알림.

### Key Components
*   **`HomeViewModel`:** 파이프라인 상태 모니터링 및 5단계 트리거 담당. (`monitorAnalysisStatus`)
*   **`ImageContentClassifier`:** `Defaulting to OBJECT` 정책으로 사물 인식률 개선.
