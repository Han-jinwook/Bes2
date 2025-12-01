# Best2 앱 개발 계획 (PLAN.md) - v6.9

Date: 2025-12-02.  01:18
Version: v6.9 (The Safety Upgrade)

1. 앱의 핵심 목표

한 줄 요약: "찍고 잊어도, 하루 사진은 스스로 정리되어 2장만 남는다. 나는 마지막에 30초만 확인하면 끝."

가치 제안: "나는 여전히 마음껏 찍는다. 대신 앱이 먼저 정리하고 나는 마지막에 두 번만 누른다."

제공 가치: 시간 절약, 마음의 여유, 저장 공간 확보, 결정 피로 감소, 안전한 보관(휴지통 및 클라우드 백업)

1.5. 앱의 핵심 원칙 (Bes2의 약속)

조용한 작동 (Silent Butler): 사용자의 사진 촬영 경험을 방해하지 않고, 백그라운드에서 눈에 띄지 않게 작동한다.

지능적인 기다림 (Intelligent Patience): 사진 촬영이 일단락되었다고 판단될 때까지 지능적으로 기다린 후 분석을 시작한다.

최소한의 개입 유도 (Minimal Interruption): 사용자의 확인이 필요한 경우에만 가장 적절한 방식으로 검토를 요청한다.

결정 피로 최소화 (Effortless Decision): 복잡한 선택 과정 없이, 최소한의 터치로 사진 정리를 완료할 수 있도록 돕는다.

안전과 본질 우선 (Safety & Essence): 부정확한 AI 추측보다 확실한 규칙을 우선하며, 앱의 본질(정리)을 해치는 기능은 과감히 제거한다.

계획 우선, 실행은 승인 후 (Plan First, Act on Approval): 모든 코드 수정 전, 이 문서의 규칙에 따라 변경 사항을 보고하고, 반드시 사용자에게 허락을 받은 후에만 작업을 진행한다.

2. 주요 기능 및 현재 상태

✅ 완료된 기능 (v6.9 Stabilization)

파이프라인 정상화 (Pipeline Normalized):

입구 개방: `GalleryRepository`의 복잡한 SQL 필터링을 제거하여 Android 15 등 최신 기기 호환성 확보.

분류 강화: `PhotoDiscoveryWorker` 내부 로직으로 스크린샷/AI 문서를 정밀하게 분류하여 Main/Trash DB로 분배.

즉시 반응성 (Instant UI): 앱 실행 즉시 `MediaStore`를 직접 조회하여 스크린샷 개수를 표시 (지연 시간 0).

DB 및 파이프라인 구조 (The Reform):

이원화 시스템: 본채(ReviewItem)와 별채(TrashItem)의 물리적 분리로 데이터 간섭 원천 차단.

안정화: Memory Recall, Gallery Diet, Trash Cleaning 각 기능의 독립적 작동 보장.

지능형 클러스터링 (Logic Upgrade):

3분 윈도우 + 얼굴 매칭(Face Matching) + 고아 병합(Orphan Merge).

검토 프로세스 개선:

Swipe Navigation ('자유 탐색' 구조).

안전한 스마트 분류 (Safety First):

사람 우선(Face First) 정책: 얼굴 감지 시 무조건 일반 사진으로 분류하여 오분류 사고 원천 차단.

쓰레기 정리 고도화: 스크린샷 우선 노출 -> DB 쓰레기 노출 순서 적용 및 무한 리필 UI 구현.

추억 소환 (Logic Optimization):

MemoryEventWorker를 통한 백그라운드 선행 분석 및 시스템 시간대 적용.

앱 경량화: AI 검색 및 불필요한 장식 요소 제거.

수익화: Google AdMob 연동.

⛔️ 철수된 기능 (Deprecation)

AI 자연어 검색: 배터리 소모 및 성능 이슈로 기능 삭제.

불필요한 메타데이터: 부정확한 칭찬 문구 등 본질과 무관한 데이터 처리 로직 제거.

🔄 부분 구현

클라우드 동기화: Google Photos 로그인/로그아웃 및 자동 백업.

3. 핵심 규칙 및 정책 (The Guardrails)

📸 사진 처리 파이프라인 (Data Flow Pipeline) - v6.9 상세 명세

0단계: 데이터 분기 및 저장 (The Dispatcher)

담당 엔진: PhotoDiscoveryWorker (물류 소장)

입구 (Fetch): `GalleryRepository`가 조건 없이 모든 과거 사진을 가져옴.

분류 (Filter): Worker 내부 로직으로 판단.

경로에 'Screenshot' 포함 or AI 판단 '문서/사물' (단, 사람 제외) -> **별채 (TrashItemEntity)**.

그 외 일반 사진 -> **본채 (ReviewItemEntity)** (source_type = DIET).

1단계: 정밀 분석 (Deep Analysis)

담당 엔진: PhotoAnalysisWorker (정밀 검사관)

대상: 본채의 status = NEW 항목.

수행: 화질(Blur), 눈 감음(Eye), 구도(NIMA), 역광(Backlight) 평가.

상태 전이: NEW -> ANALYZED (성공) 또는 STATUS_REJECTED (실패).

2단계: 클러스터링 (Grouping)

담당 엔진: ClusteringWorker (포장 담당)

대상: 본채의 ANALYZED 또는 STATUS_REJECTED 항목.

로직:

Time Window: 3분 단위로 확장하여 동일 상황(Scene) 인식.

Face Matching: 시각적 차이에도 불구, '동일 인물' 감지 시 병합.

Orphan Merge: 1~2장의 독립 사진(Outliers)을 인접 클러스터로 강제 병합.

상태 전이: ANALYZED -> CLUSTERED.

3단계: 검토 및 완료 (Review & Finish)

담당 엔진: ReviewViewModel (전시관)

탐색: HorizontalPager를 통한 자유로운 Swipe.

상태 전이: 사용자의 선택에 따라 CLUSTERED -> KEPT (보관) 또는 DELETED (삭제).

⏱️ 추억 소환 로직 (Memory Recall)

담당 엔진: MemoryEventWorker (추억 담당관)

독립성: 본채(ReviewItemEntity)에 저장하되, source_type = MEMORY 태그를 사용하여 다이어트(DIET) 데이터와 완벽히 격리.

상태: EVENT_MEMORY 상태로 관리.

🚫 품질 게이트 규칙 (Thresholds)

눈 감음: 눈 뜸 확률 0.3 미만 (엄격).

흐림: 선명도 점수(Laplacian) 30.0 미만.

역광: 얼굴 영역 평균 밝기가 배경보다 현저히 낮음.

☁️ 자동 동기화 규칙

원칙: 신뢰도 높은 OneTimeWorkRequest 체이닝(Chaining) 방식.

정책: 앱 설치 시 '바로바로 동기화(IMMEDIATE)' 모드 기본 적용.

💯 점수 계산 공식

Score = (MUSIQ * 0.5) + (NIMA * 0.3) + (Smile_Prob * 30 or -10) + (Base * 0.2)

4. 아키텍처 및 데이터 구조

아키텍처: Clean Architecture 변형 (Domain, Data, Presentation Layer 분리).

기술 스택: Hilt, Jetpack Compose, Coroutines & Flow, WorkManager.

🗄️ 데이터베이스 명세 (Schema) - v6.9 Updated

1. ReviewItemEntity (본채 - 소중한 사진)

용도: 정밀 분석이 필요한 핵심 데이터.

핵심 필드:

source_type: DIET (다이어트), MEMORY (추억), INSTANT (촬영 직후-Future) [핵심 격리 장치]

status: NEW -> ANALYZED/REJECTED -> CLUSTERED -> KEPT/DELETED

scores: nimaScore, blurScore, musiqScore 등.

2. TrashItemEntity (별채 - 쓰레기)

용도: 분석 없이 빠르게 비울 데이터 (스크린샷, 문서).

핵심 필드:

status: READY, DELETED

3. ImageClusterEntity

용도: 클러스터링 메타데이터 (ReviewItem들을 묶는 그룹 정보).

5. 주요 개발 히스토리 (Milestones)

Milestone 15: Trash Cleaning Upgrade (v6.9 완료)

Logic: 오분류 방지를 위한 '사람 우선(Face First)' 정책 적용 (ImageContentClassifier).

UX: 스크린샷 정리 시 진짜 스크린샷 우선 노출 및 무한 리필(Infinite Refill) UI 구현.

Milestone 14: Pipeline Stabilization (v6.8 완료)

Logic Fix: Android 15 호환성을 위한 Query 로직 단순화 및 Worker 기반 분류 체계 확립.

UI Response: 앱 실행 시 갤러리 스크린샷 즉시 카운팅 구현 (UX 개선).

6. 다음 목표 (Next Mission) - POST MVP

1. 커뮤니티 및 운영 (Growth) - [Priority: High]

자사 커뮤니티 홍보 (Rolling Banner):

계획: 설정 화면 최하단에 Firebase Remote Config를 연동하여 롤링 배너 구현.

2. 미래 기술 (Future Tech) - [Long Term]

개인화 모델: 사용자 피드백 기반의 맞춤형 점수 모델 학습.

구독 모델: 정식 런칭 이후 검토.

3. 브랜딩 및 ASO (Brand Identity) - [Priority: Medium]

공통 헤더 로고 통일: 모든 주요 화면 상단에 Bes2 로고 고정 배치.

결과 화면 워터마크: "Organized by Bes2" 문구 자동 포함.

7. 협업 원칙

총감독: 최종 결정권자 (사용자).

재미나이: 캔버스 plan.md 관리자 (PM/Gemini).

AS에이전트: Android Studio AI 어시턴트.

7.1. AS에이전트 원칙

기능 동결: 허락 없이 기존 기능/UI 수정 금지.

선 계획, 후 작업: 변경 계획 보고 -> 승인 -> 작업.

결과 보고: 변경 사항 텍스트 요약 보고 (파일 직접 수정 금지).

Git 커밋: "제목 1줄, 내용 2줄" 형식 준수.

7.2. 재미나이 원칙

Plan.md 관리: 총감독 컨펌 후 캔버스 파일 수정.

문서 본질 유지: 기능 및 로직 정의서의 본질 유지. 지엽적 UI 배제.
