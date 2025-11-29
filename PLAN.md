Best2 앱 개발 계획 (PLAN.md) - v6.4

Date: 2025-11-27

1. 앱의 핵심 목표

한 줄 요약: "찍고 잊어도, 하루 사진은 스스로 정리되어 2장만 남는다. 나는 마지막에 30초만 확인하면 끝."

가치 제안: "나는 여전히 마음껏 찍는다. 대신 앱이 먼저 정리하고 나는 마지막에 두 번만 누른다."

제공 가치: 시간 절약, 마음의 여유, 저장 공간 확보, 결정 피로 감소, 안전한 보관(휴지통 및 클라우드 백업)

1.5. 앱의 핵심 원칙 (Bes2의 약속)

조용한 작동 (Silent Butler): 사용자의 사진 촬영 경험을 방해하지 않고, 백그라운드에서 눈에 띄지 않게 작동한다.

지능적인 기다림 (Intelligent Patience): 사진 촬영이 일단락되었다고 판단될 때까지 지능적으로 기다린 후 분석을 시작한다.

최소한의 개입 유도 (Minimal Interruption): 사용자의 확인이 필요한 경우에만 가장 적절한 방식으로 검토를 요청한다.

결정 피로 최소화 (Effortless Decision): 복잡한 선택 과정 없이, 최소한의 터치로 사진 정리를 완료할 수 있도록 돕는다.

자동화 우선 (Automation First): 사용자가 신경 쓰지 않아도 대부분의 작업이 자동으로 이루어지도록 설계한다.

계획 우선, 실행은 승인 후 (Plan First, Act on Approval): (신규 규칙) 모든 코드 수정 전, 이 문서의 규칙에 따라 변경 사항을 보고하고, 반드시 사용자에게 허락을 받은 후에만 작업을 진행한다.

2. 주요 기능 및 현재 상태

✅ 완료된 기능

자동 사진 정리: 사진 변경 감지 및 신규 사진 DB 저장.

클러스터링: (상세 규칙은 3. 핵심 규칙 섹션 참조)

사진 분석 및 평가:

기본 품질 필터 (눈 감음, 흐림 정도).

감성 품질 평가: MUSIQ (예술 점수) 및 NIMA (기술 점수) 하이브리드 평가.

웃음 확률 분석 (ML Kit Vision API 활용).

역광 감지 (Backlighting): 얼굴/배경 밝기 비교 분석.

스마트 분류 (Content Type): 추억(Memory) vs 문서(Document) 자동 분류.

자연어 검색 (Semantic Search): MobileCLIP 기반 문장 검색 ("웃는 아기").

간편 검토 인터페이스:

분석 완료 후, 검토가 필요한 클러스터가 있으면 사용자에게 알림 전송.

검토 화면 구성:

베스트 1, 2: 점수 기준으로 선정된 베스트 사진 2장 표시 (칭찬 뱃지 포함).

나머지: 베스트 사진으로 선정되지 않은 정상 사진 목록 표시.

실패: 분석 실패 사진 목록 표시. 실패 사유(눈감음, 흐림, 역광)를 명확히 텍스트로 표기하고 '심폐소생(복구)' 버튼 제공.

수익화 (Monetization): Google AdMob 연동 (띠 배너, 전면 광고).

🔄 부분 구현

클라우드 동기화: Google Photos 로그인/로그아웃 및 자동 백업, 수동 동기화 실행 기능.

3. 핵심 규칙 및 정책 (The Guardrails)

📸 사진 처리 파이프라인 (Data Flow Pipeline) - v6.4 고도화

0단계: 콘텐츠 분류 (ImageContentClassifier)

ML Kit을 사용하여 사진을 **'추억(MEMORY)'**과 **'문서(DOCUMENT)'**로 1차 분류한다.

추억(MEMORY): 인물, 음식, 풍경 등 -> 1단계(클러스터링)로 진입하여 정밀 분석.

문서(DOCUMENT): 영수증, 문서, 화이트보드 등 -> 정밀 분석을 건너뛰고 '문서 정리' 대상으로 분류(별도 관리).

1단계: 클러스터링 (ClusteringWorker)

새로운 사진(NEW 상태)이 감지되면, ClusteringWorker가 가장 먼저 실행된다.

스크린샷 제외 규칙: 파일 경로에 'Screenshot', 'Capture' 등이 포함된 이미지는 제외하고 즉시 IGNORED 상태로 처리한다.

pHash(임계값 15)를 직접 계산하여 비슷한 사진들을 하나의 '클러스터(묶음)'로 정교하게 묶고(얼굴/사물 구분), 각 사진에 클러스터 ID를 부여한다.

완료된 사진들의 상태를 PENDING_ANALYSIS로 변경하고, PhotoAnalysisWorker를 호출한다.

2단계: 분석 및 상태 부여 (PhotoAnalysisWorker)

PENDING_ANALYSIS 상태의 사진들을 가져와 클러스터 단위로 분석을 시작한다.

자동 충전 로직: 홈 화면 진입 시(ON_RESUME) 부족한 분석 물량을 체크하고 자동으로 채워 넣는다. (목표: 30장 유지)

백그라운드 모드: isBackgroundDiet 플래그가 true일 경우, 알림 없이 조용히 READY_TO_CLEAN 상태로 저장한다.

품질 게이트 (v5.6 강화): '눈 감음', '흐림', **'역광'**을 검사하여, 기준 미달인 사진은 즉시 STATUS_REJECTED(실패) 상태로 변경하고 더 이상의 점수 계산을 중단한다.

임베딩 추출 (v6.5 신규): MobileCLIP을 사용하여 사진의 특징 벡터(Embedding)를 추출하고 DB에 저장한다. (자연어 검색용)

점수 계산 (v6.4 업데이트): MUSIQ(예술성), NIMA(기술성), 웃음 확률을 종합하여 최종 점수를 산출하고, 상태를 ANALYZED(성공)로 변경한다.

모든 분석이 완료되면 사용자에게 검토 알림을 보낸다.

3단계: 점수화 및 UI 분리 표시 (ReviewViewModel & Screen) - v6.4 고도화

검토 화면에서는 ANALYZED 상태의 사진들만을 대상으로 최종 점수를 계산하여 '베스트'와 '나머지'를 선정한다.

STATUS_REJECTED 사진은 '실패' 섹션으로 분리하여 보여준다.

베스트 칭찬: 베스트 사진 하단에 선정 사유(예: "보기 좋은 미소 😊")를 뱃지로 표시.

심폐소생 (하이브리드 복원): 실패 사진 확대 시 [복구] 버튼 제공.

ESRGAN: 전체 해상도 4배 확대 및 노이즈 제거.

GFPGAN: 흐릿해진 얼굴 이목구비(눈/코/입) 선명하게 재건.

성공 시 '나머지' 섹션으로 승격.

⏱️ 분석 시작 지연 규칙 (지능적인 기다림) - v3.6 신규

원칙: 사용자의 사진 촬영이 일단락될 때까지 대기.

트리거: 마지막 사진 촬영(감지) 후, '설정'에서 지정한 시간 동안 추가 촬영이 없으면 ClusteringWorker 트리거.

현재 테스트 값: 기본값 1분. (추후 설정 UI에 옵션 추가 예정)

🚫 품질 게이트 규칙 (실패 사진 기준) - v5.8 수정 (복귀)

눈 감음 (Eyes Closed): 양쪽 눈 중 어느 한쪽이라도 "눈을 떴을 확률"이 0.5 미만일 경우 실패 처리.

흐림 (Blur): 선명도 점수(Laplacian)가 30.0 미만일 경우 실패 처리.

역광 (Backlighting): 얼굴이 배경보다 현저히 어둡거나 절대 밝기가 낮을 경우 실패 처리.

☁️ 자동 동기화 규칙 - v4.7 최적화

원칙: 신뢰도 높은 OneTimeWorkRequest를 체인 형태로 연결하여 안정성 보장 ('릴레이' 방식).

실행 순서: SettingsViewModel에서 최초 예약 -> DailyCloudSyncWorker가 작업 완료 후 다음 날 작업 스스로 예약.

기본값: 앱 설치 시 '바로바로 동기화(IMMEDIATE)' 적용.

🧩 클러스터링 규칙 - v2.6 신규

기준: pHash 값 사이의 해밍 거리(Hamming Distance).

임계값: 해밍 거리 15 이하인 경우 유사한 사진으로 판단하여 묶음.

💯 점수 계산 규칙 (베스트 사진 선정) - v6.4 고도화

기본 점수 (Total 100):

MUSIQ (예술 점수): 비중 50% (가장 중요).

NIMA (기술 점수): 비중 30%.

기타: 20%.

가산점/감점:

가산점: 웃음 확률 * 30 (웃는 얼굴 우대).

감점: 웃음 확률 0.1 미만 시 -10점 (찡그린 얼굴 필터링).

공식: 최종 점수 = (MUSIQ * 5 + NIMA * 3) + (웃음 확률 * 30 또는 -10)

4. 아키텍처 및 데이터 구조

아키텍처: Clean Architecture 변형 (app, data, domain, background, ml 등 모듈 분리).

기술 스택: Hilt, Jetpack Compose, Coroutines & Flow.

데이터베이스 (Room Entities):

ImageItemEntity: id, uri, pHash, scores(NIMA/MUSIQ), embedding, 눈감음/웃음 여부, clusterId, status 등.

ImageClusterEntity: id, creationTime, reviewStatus.

5. 주요 개발 히스토리 (Milestones)

Milestone 10: Smart Classification & Logic Upgrade (v5.9)

스마트 분류 (ML Kit): 추억 vs 문서.

역광 감지 및 이미지 심폐소생(TFLite ESRGAN) 구현.

감성 품질 강화 (칭찬 뱃지) 및 수익화(AdMob) 구현.

UI 고도화: 추억 소환, 미니맵, 스마트 워터폴.

Milestone 11: Semantic Intelligence & Home Evolution (v6.4)

자연어 검색 (MobileCLIP ONNX): 텍스트-이미지 벡터 매칭.

예술 점수 심사 (MUSIQ): 비중 50% 적용.

하이브리드 복원 엔진 (ESRGAN + GFPGAN).

홈 UI 진화 (2.0): 2x2 그리드 카드, 타자기 효과.

6. 다음 목표 (Next Mission) - POST MVP (v6.4)

1. 커뮤니티 및 운영 (Growth) - [Priority: High]

자사 커뮤니티 홍보 (Rolling Banner):

상태: 미구현 (베타 런칭 전 마지막 과제).

계획: 설정 화면 최하단에 Firebase Remote Config를 연동하여 롤링 배너 구현. 베타 테스터 피드백 수집 및 카페 유입 유도.

2. 미래 기술 (Future Tech) - [Long Term]

사용자 취향 학습 (ML): 복구된 사진 데이터를 기반으로 개인화된 판정 기준 학습.

구독 모델: 정식 런칭 이후 검토.

7. 협업 원칙 (v3.7 - 신규 절차)

총감독: 최종 결정권자 (사용자).

재미나이: 캔버스 plan.md 관리자 (PM/Gemini, 본인).

AS에이전트: Android Studio AI 어시턴트 (윈드서퍼 에이전트).

7.1. AS에이전트 (Android Studio Agent) 원칙

기능 동결: 허락 없이 기존 기능/UI 수정 금지.

선 계획, 후 작업: 변경 계획을 먼저 보고하고 승인(컨펌) 후 작업.

결과 보고: PLAN.md 직접 수정 금지. 변경 사항은 텍스트로 요약 보고.

Git 커밋 보고: 작업 완료 후 터미널 명령어(커밋 메시지 포함) 보고.

로그/필터: 긴 로그 요청 금지. 필요한 필터명만 제시.

7.2. 재미나이 (Gemini) 원칙

Plan.md 수정 절차: 변경 사항에 대한 '요약 보고' 제안 -> 총감독 컨펌 -> 캔버스 파일 수정.

수동 반영: Git 명령어 제공하지 않음. 파일 반영은 총감독이 수행.