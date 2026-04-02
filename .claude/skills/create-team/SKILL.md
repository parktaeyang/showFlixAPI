---
name: create-team
description: 팀장, 기획자/리뷰어, 개발자, QA로 구성된 개발팀을 생성합니다. 팀 생성 후 각 팀원을 분할창으로 표시합니다.
disable-model-invocation: true
allowed-tools: TeamCreate, Agent, SendMessage, TaskCreate, Read
---

# 개발팀 생성

$ARGUMENTS 프로젝트를 위한 개발팀을 생성합니다.

## 팀 생성 절차

### 1단계: 팀 생성
TeamCreate로 팀을 생성합니다.
- team_name: `$ARGUMENTS` (인자가 없으면 `dev-team`을 사용)
- description: "개발팀 - 팀장, 기획자/리뷰어, 개발자, QA"

### 2단계: 팀원 생성 (3명을 동시에 Agent로 스폰)

TeamCreate를 실행한 메인 세션이 자동으로 **팀장** 역할을 합니다. 별도 팀장 에이전트를 스폰하지 않습니다.

모든 팀원을 **동시에 병렬로** 스폰합니다:

1. **기획자/리뷰어** (planner-reviewer)
   - Agent tool: `name: "planner-reviewer"`, `subagent_type: "Plan"`, `team_name: "{팀이름}"`
   - **읽기 전용 에이전트** (Plan 타입 사용으로 토큰 절약)
   - 프롬프트: "당신은 기획자 겸 리뷰어입니다. .claude/agents/planner-reviewer.md의 지침을 따르세요. 코드를 직접 수정하지 않고 분석과 리뷰만 수행합니다. TaskList를 확인하고 배정된 작업을 기다리세요."
   - `run_in_background: true`

2. **개발자** (developer)
   - Agent tool: `name: "developer"`, `subagent_type: "code-writer"`, `team_name: "{팀이름}"`
   - 프롬프트: "당신은 개발자입니다. .claude/agents/code-writer.md의 지침을 따르세요. 계획서를 기반으로 코드를 구현합니다. TaskList를 확인하고 배정된 작업을 기다리세요."
   - `run_in_background: true`

3. **QA** (qa)
   - Agent tool: `name: "qa"`, `subagent_type: "test-runner"`, `team_name: "{팀이름}"`
   - 프롬프트: "당신은 QA입니다. .claude/agents/qa-agent.md의 지침을 따르세요. 테스트 작성과 실행, 품질 검증을 담당합니다. TaskList를 확인하고 배정된 작업을 기다리세요."
   - `run_in_background: true`

### 3단계: 팀 구성 완료 안내

팀 생성이 완료되면 사용자에게 다음을 안내합니다:

```
개발팀이 생성되었습니다!

| 역할 | 이름 | 타입 | 권한 |
|------|------|------|------|
| 팀장 | 메인 세션 (나) | - | 전체 (자동) |
| 기획자/리뷰어 | planner-reviewer | Plan (읽기전용) | 읽기 |
| 개발자 | developer | code-writer | 전체 |
| QA | qa | test-runner | 편집/실행 |

**분할창 전환**: Shift+Down으로 팀원 간 전환
**태스크 목록**: Ctrl+T로 확인

요구사항을 입력하시면 팀장(메인 세션)이 작업을 분배합니다.
```

## 주의사항
- 기획자/리뷰어는 Plan 타입으로 스폰하여 읽기 권한만 부여 (토큰 절약)
- 모든 팀원은 백그라운드로 스폰하여 병렬 실행
- 팀원 이름(name)으로 SendMessage를 통해 소통 가능
