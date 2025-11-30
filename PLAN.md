Best2 앱 개발 계획 (PLAN.md) - v6.6

Date: 2025-11-30
Version: v6.6 (The Structural Reform)

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

✅ 완료된 기능 (v6.6 Update)

DB 및 파이프라인 구조 개혁 (The Reform):

DB 물리적 분리: '추억/다이어트(본채)'와 '쓰레기(별채)' DB를 분리하여 데이터 납치 및 간섭 원천 차단.

안정화: Memory Recall(74장), Gallery Diet(30장), Cleaning(50장) 각 기능 독립적 정상 작동 확인.

지능형 클러스터링 (Logic Upgrade):

3분 윈도우 + 얼굴 매칭(Face Matching) + 고아 병합(Orphan Merge) 알고리즘 적용.

검토 프로세스 개선:

Swipe Navigation 적용으로 '자유 탐색' 구조로 변경.

안전한 스마트 분류:

시스템 폴더 경로 기반의 결정론적(Deterministic) 분류 및 DB 자가 치유(Self-Healing).

추억 소환 (Logic Optimization):

백그라운드 선행 분석 및 시스템 시간대 적용.

앱 경량화: AI 검색 및 불필요한 장식 요소 제거.

수익화: Google AdMob 연동.

⛔️ 철수된 기능 (Deprecation)

AI 자연어 검색: 배터리 소모 및 성능 이슈로 기능 삭제.

불필요한 메타데이터: 부정확한 칭찬 문구 등 본질과 무관한 데이터 처리 로직 제거.

🔄 부분 구현

클라우드 동기화: Google Photos 로그인/로그아웃 및 자동 백업.

3. 핵심 규칙 및 정책 (The Guardrails)

📸 사진 처리 파이프라인 (Data Flow Pipeline) - v6.6 아키텍처 반영

0단계: 데이터 분기 및 저장 (Dispatcher & DB Separation)

물리적 분리 원칙:

ReviewItemEntity (본채): 갤러리 다이어트(최신) 및 추억 소환(과거) 데이터 전용. source_type 필드로 기능 간 영역을 엄격히 구분.

TrashItemEntity (별채): 스크린샷, 캡처 등 정리 대상 파일 전용. 메인 로직과 데이터 간섭 완전 차단.

스캔 및 분류: PastPhotoAnalysisWorker가 스캔 즉시 '정상'과 '쓰레기'를 분류하여 각자의 DB(본채/별채)로 라우팅.

카운팅 로직: 목표치(예: 30장) 충족 시까지 **'쓰레기는 카운트에서 제외'**하고 스캔을 지속하여 유효 데이터 확보 보장.

1단계: 클러스터링 (Advanced Clustering Logic)

시간 임계값 (Time Window): 3분 단위로 확장하여 동일 상황(Scene) 인식.

얼굴 인식 병합 (Face Matching): '동일 인물' 감지 시 시각적 차이에도 불구하고 병합.

고아 병합 (Orphan Merge): 1~2장의 독립 사진(Outliers)을 인접 클러스터로 강제 병합.

2단계: 분석 및 안정화 (Stabilized Analysis)

예외 처리 강화 (Fault Tolerance): 이미지 로딩 실패(Exif 오류 등) 발생 시, 프로세스를 죽이지 않고 로그 기록 후 즉시 다음 사진으로 Skip.

파이프라인 강제 가동: 앱 실행 시 멈춰있는 분석/클러스터링 작업이 감지되면 REPLACE 정책으로 강제 재시작하여 데이터 흐름 유지.

점수 계산: MUSIQ(예술성), NIMA(기술성), 웃음 확률 가중치 합산.

3단계: 검토 로직 (Review Flow)

탐색 구조: HorizontalPager를 통한 자유로운 Swipe 탐색.

UI 반응성: 분석 완료 즉시 버튼 활성화 및 기능별(추억/다이어트/청소) 독립적 UI 갱신.

⏱️ 추억 소환 로직 (Memory Recall)

독립성 보장: ReviewItemEntity 내에서 다이어트 기능과 섞이지 않도록 격리.

시간 기준: ZoneId.systemDefault()를 사용하여 로컬 날짜 기준 쿼리.

🚫 품질 게이트 규칙 (Thresholds)

눈 감음: 눈 뜸 확률 0.5 미만.

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

데이터베이스 (Room) - v6.6 변경:

ReviewItemEntity (Main): id, uri, clusterId, scores, source_type (DIET/MEMORY) 등 핵심 데이터.

TrashItemEntity (Annex): id, uri, trashType (SCREENSHOT/BLUR) 등 정리 대상 데이터.

ImageClusterEntity: 클러스터링 메타데이터.

5. 주요 개발 히스토리 (Milestones)

Milestone 13: The Structural Reform (v6.6 완료)

DB Separation: 단일 테이블 구조의 한계(데이터 간섭)를 극복하기 위해 Main/Trash DB 물리적 분리 완료.

Pipeline Normalization: 스캔-분류-저장 로직 재설계로 추억 소환(74장), 다이어트(30장) 기능의 완벽한 독립성 확보.

Analysis Stabilization: 예외 처리 및 강제 재시작 로직 추가로 '무한 로딩' 및 '중단' 현상 해결.

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