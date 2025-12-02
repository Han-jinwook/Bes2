# Bes2 개발 계획서 (v7.0 - Final Architecture)

**Date:** 2025-12-03
**Status:** Core Logic Stabilized & Ready for Polish
**Author:** Han-jinwook

---

## 📅 Development Log

### ✅ v7.0: Core Logic Stabilization (2025-12-03)
*   **Pipeline Overhaul:**
    *   `PhotoDiscoveryWorker`: `Cursor` 기반 스트림 처리로 변경 (전수 스캔, 누락 없음).
    *   `PhotoAnalysisWorker`: 배치 루프 처리로 변경 (전수 분석, OOM 방지).
    *   `GalleryRepository`: 날짜 필터 제거 및 단순화.
*   **Safety First:**
    *   `ImageContentClassifier`: 애매하면 `MEMORY`로 분류 (군복/야간 사진 구제).
    *   `EyeClosedDetector`: 얼굴 크기 3% 미만 시 눈 감음 패스.
    *   `PhotoAnalysisWorker`: 흐림 기준 20.0으로 완화.
*   **UI/UX:**
    *   **Infinite Refill:** DB 선행 확보를 통한 무로딩 경험 구현.
    *   **Report:** `SettingsRepository` 기반 활동량 카운터 적용 (오늘 정리한 개수 정확도 100%).
    *   **Ads:** 30장 기준, 화면별 독립 카운팅 적용.
    *   **Permissions:** 불필요한 카메라 권한 제거.

---

## 🚀 Next Steps (Tomorrow)

### 1. 최종 안정화 및 테스트
*   **대용량 테스트:** 사진 5,000장 이상 기기에서 발열 및 배터리 소모 체크.
*   **예외 케이스:** 권한 거부 시, DB 손상 시 복구 로직 확인.

### 2. UI 폴리싱 (Polishing)
*   **텅 빈 화면 처리:** Worker가 도는 동안(초기 1~2분) 사용자에게 보여줄 "열심히 분석 중입니다" 애니메이션이나 안내 문구 강화.
*   **튜토리얼:** "전체 스캔 모드"에 대한 안내 (처음 한 번만 오래 걸려요).

### 3. 출시 준비
*   `proguard-rules.pro` 확인 (ML Kit 등).
*   버전 코드 올리기.

---

## 🏗️ Architecture Overview

### Data Flow
1.  **Scanner (`PhotoDiscoveryWorker`):** `MediaStore` -> `Cursor` -> `ReviewItemDao` (NEW) / `TrashItemDao` (READY).
2.  **Analyzer (`PhotoAnalysisWorker`):** `NEW` items -> AI Check -> `ANALYZED` / `REJECTED`.
3.  **Clusterer (`ClusteringWorker`):** `ANALYZED` -> `ImageClusteringHelper` -> `CLUSTERED`.
4.  **UI (`ViewModel`):** `CLUSTERED` items -> User Action -> `KEPT` / `DELETED` -> `SettingsRepository` (Count++).

### Key Components
*   `SettingsRepository`: Daily stats counters, last scan timestamps.
*   `ImageContentClassifier`: Safety-first classification logic.
*   `EyeClosedDetector`: Small face rescue logic.
