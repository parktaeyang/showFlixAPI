---
name: create-team
description: 팀장, 프론트엔드 개발자, 백엔드 개발자, 리뷰어로 구성된 개발팀을 생성합니다. 팀 생성 후 각 팀원을 분할창으로 표시합니다.
disable-model-invocation: true
allowed-tools: TeamCreate, Agent, SendMessage, TaskCreate, Read
---

# 개발팀 생성

$ARGUMENTS 프로젝트를 위한 개발팀을 생성합니다.

## 팀 생성 절차

### 1단계: 팀 생성
TeamCreate로 팀을 생성합니다.
- team_name: `$ARGUMENTS` (인자가 없으면 `dev-team`을 사용)
- description: "개발팀 - 팀장, 프론트엔드 개발자, 백엔드 개발자, 리뷰어"

### 2단계: 팀원 생성 (3명을 동시에 Agent로 스폰)

TeamCreate를 실행한 메인 세션이 자동으로 **팀장** 역할을 합니다. 별도 팀장 에이전트를 스폰하지 않습니다.

모든 팀원을 **동시에 병렬로** 스폰합니다:

1. **프론트엔드 개발자** (frontend-dev)
   - Agent tool: `name: "frontend-dev"`, `subagent_type: "code-writer"`, `team_name: "{팀이름}"`
   - 프롬프트: "당신은 프론트엔드 개발자입니다. .claude/agents/code-writer.md의 지침을 따르세요. 프론트엔드(HTML, CSS, JavaScript, React) 구현을 담당합니다. 계획서를 기반으로 UI 컴포넌트와 화면을 구현합니다. TaskList를 확인하고 배정된 작업을 기다리세요."
   - `run_in_background: true`

2. **백엔드 개발자** (backend-dev)
   - Agent tool: `name: "backend-dev"`, `subagent_type: "code-writer"`, `team_name: "{팀이름}"`
   - 프롬프트: "당신은 백엔드 개발자입니다. .claude/agents/code-writer.md의 지침을 따르세요. 백엔드(Java, Spring Boot, MyBatis, SQL) 구현을 담당합니다. 계획서를 기반으로 Controller, Service, Mapper, Domain, SQL을 구현합니다. TaskList를 확인하고 배정된 작업을 기다리세요."
   - `run_in_background: true`

3. **리뷰어** (reviewer)
   - Agent tool: `name: "reviewer"`, `subagent_type: "Plan"`, `team_name: "{팀이름}"`
   - **읽기 전용 에이전트** (Plan 타입 사용으로 토큰 절약)
   - 프롬프트: "당신은 리뷰어입니다. .claude/agents/reviewer.md의 지침을 따르세요. 코드를 직접 수정하지 않고 분석과 리뷰만 수행합니다. 구현된 코드의 품질, 일관성, 버그 여부를 검토합니다. TaskList를 확인하고 배정된 작업을 기다리세요."
   - `run_in_background: true`

### 3단계: 팀 구성 완료 안내

팀 생성이 완료되면 사용자에게 다음을 안내합니다:

```
개발팀이 생성되었습니다!

| 역할 | 이름 | 타입 | 권한 |
|------|------|------|------|
| 팀장 | 메인 세션 (나) | - | 전체 (자동) |
| 프론트엔드 개발자 | frontend-dev | code-writer | 전체 |
| 백엔드 개발자 | backend-dev | code-writer | 전체 |
| 리뷰어 | reviewer | Plan (읽기전용) | 읽기 |

**분할창 전환**: Shift+Down으로 팀원 간 전환
**태스크 목록**: Ctrl+T로 확인

요구사항을 입력하시면 팀장(메인 세션)이 작업을 분배합니다.
```

## 워크플로우

팀장(메인 세션)은 다음 순서로 작업을 진행합니다:

1. 계획서 확인 → 프론트엔드/백엔드 개발자에게 동시 작업 배분
2. 프론트엔드 개발자: UI/화면 구현 (HTML, CSS, JS, React)
3. 백엔드 개발자: API/DB/서비스 구현 (Java, Spring Boot, MyBatis)
4. 개발 완료 → 리뷰어에게 코드 리뷰 요청
5. 리뷰 피드백 → 해당 개발자에게 수정 요청
6. 리뷰 승인 → 완료 보고

## 주의사항
- 리뷰어는 Plan 타입으로 스폰하여 읽기 권한만 부여 (토큰 절약)
- 프론트엔드/백엔드 개발자는 code-writer 타입으로 스폰하여 전체 권한 부여
- 모든 팀원은 백그라운드로 스폰하여 병렬 실행
- 팀원 이름(name)으로 SendMessage를 통해 소통 가능
- 계획서는 local-notes 디렉토리의 plan.md 파일을 참고
