# Bes2 개발 계획서 (v7.1 - Final Polish & UX Upgrade)

**Date:** 2025-12-03
**Status:** Core Logic Stabilized & UI/UX Polished
**Author:** Han-jinwook

---

## 📅 Development Log

### ✅ v7.1: UX Upgrade & Stability (2025-12-03)
*   **Waiting UX Overhaul:**
    *   **Real-time Progress:** 다이어트 분석 시 "전체 갤러리 분석 중... (350 / 1063)" 실시간 카운팅 표시.
    *   **Stage Separation:**
        *   **1단계 (분류):** 스캔 완료 즉시 '쓰레기 정리' 카드 활성화 (AI 분석 안 기다림).
        *   **2단계 (분석):** '다이어트' 카드는 심층 분석 완료 시까지 대기 메시지 표시.
    *   **Friendly Messages:** "숨은 쓰레기 찾는 중...", "잠시만 기다려주세요" 등 친절한 안내 문구 적용.
*   **Logic Stabilization:**
    *   **Worker Policy:** `ExistingWorkPolicy.REPLACE` 적용으로 작업 취소/멈춤 현상 해결 (개발 모드).
    *   **Progress Tracking:** 1장 분석마다 즉시 DB/Settings 저장하여 UI 반응성 극대화.
*   **Memory Recall:**
    *   **Notification:** 분석 완료 시 "추억 소환 🎉" 알림 발송.
    *   **Logic:** 하루 20장 이상 촬영된 날짜 자동 감지 및 제안.

---

## 🚀 Next Steps (Tomorrow)

### 1. 최종 안정화 및 테스트
*   **대용량 테스트:** 사진 5,000장 이상 기기에서 발열 및 배터리 소모 체크.
*   **예외 케이스:** 권한 거부 시, DB 손상 시 복구 로직 확인.

### 2. 출시 준비
*   `proguard-rules.pro` 확인 (ML Kit 등).
*   버전 코드 올리기.
*   **Worker 정책 변경:** 배터리 효율을 위해 `REPLACE` -> `KEEP`으로 최종 변경 검토.

---

## 🏗️ Architecture Overview

### Data Flow
1.  **Scanner (`PhotoDiscoveryWorker`):** `MediaStore` -> `Cursor` -> `ReviewItemDao` (NEW) / `TrashItemDao` (READY).
    *   *Update:* 전체 스캔 개수(`totalScanCount`) 즉시 저장.
2.  **Analyzer (`PhotoAnalysisWorker`):** `NEW` items -> AI Check -> `ANALYZED` / `REJECTED`.
    *   *Update:* 1장마다 `analysisProgressCurrent` 저장 (실시간 카운팅).
3.  **Clusterer (`ClusteringWorker`):** `ANALYZED` -> `ImageClusteringHelper` -> `CLUSTERED`.
4.  **UI (`ViewModel`):**
    *   `isDiscoveryInProgress` (스캔 중): 쓰레기 카드 대기.
    *   `isAnalysisInProgress` (분석 중): 다이어트 카드 대기 (진행률 표시).

### Key Components
*   `SettingsRepository`: Daily stats counters, last scan timestamps, **real-time analysis progress**.
*   **`GalleryRepository`:** `findLargePhotoGroups(minCount: 20)` - 하루에 20장 이상 촬영된 날짜 그룹을 찾아 '추억'으로 제안.
*   `NotificationHelper`: "추억 소환", "정리 완료" 등 상황별 알림 관리.
