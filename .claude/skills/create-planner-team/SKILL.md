---
name: create-planner-team
description: 팀장, 기획자, 리뷰어, UX담당자로 구성된 기획팀을 생성합니다. 팀 생성 후 각 팀원을 분할창으로 표시합니다.
disable-model-invocation: true
allowed-tools: TeamCreate, Agent, SendMessage, TaskCreate, Read
---

# 기획팀 생성

$ARGUMENTS 프로젝트를 위한 기획팀을 생성합니다.

## 팀 생성 절차

### 1단계: 팀 생성
TeamCreate로 팀을 생성합니다.
- team_name: `$ARGUMENTS` (인자가 없으면 `planner-team`을 사용)
- description: "기획팀 - 팀장, 기획자, 리뷰어, UX담당자"

### 2단계: 팀원 생성 (3명을 동시에 Agent로 스폰)

TeamCreate를 실행한 메인 세션이 자동으로 **팀장** 역할을 합니다. 별도 팀장 에이전트를 스폰하지 않습니다.

모든 팀원을 **동시에 병렬로** 스폰합니다:

1. **기획자** (planner)
   - Agent tool: `name: "planner"`, `subagent_type: "Plan"`, `team_name: "{팀이름}"`
   - **읽기 전용 에이전트** (Plan 타입 사용으로 토큰 절약)
   - 프롬프트: "당신은 기획자입니다. .claude/agents/planner.md의 지침을 따르세요. 요구사항을 분석하고 기획 문서를 작성합니다. 기획 문서는 local-notes 디렉토리에 작성합니다. plan.md 작성 시 요구사항, 현황 분석, 변경 계획, 상세 구현 방법(step1, step2...), 변경 범위 요약, 추가고려사항 순서를 따릅니다. TaskList를 확인하고 배정된 작업을 기다리세요."
   - `run_in_background: true`

2. **리뷰어** (reviewer)
   - Agent tool: `name: "reviewer"`, `subagent_type: "Plan"`, `team_name: "{팀이름}"`
   - **읽기 전용 에이전트** (Plan 타입 사용으로 토큰 절약)
   - 프롬프트: "당신은 리뷰어입니다. .claude/agents/reviewer.md의 지침을 따르세요. 기획안을 검토하고 피드백을 제공합니다. 코드나 문서를 직접 수정하지 않고 분석과 리뷰만 수행합니다. TaskList를 확인하고 배정된 작업을 기다리세요."
   - `run_in_background: true`

3. **UX담당자** (ux-designer)
   - Agent tool: `name: "ux-designer"`, `subagent_type: "Plan"`, `team_name: "{팀이름}"`
   - **읽기 전용 에이전트** (Plan 타입 사용으로 토큰 절약)
   - 프롬프트: "당신은 UX담당자입니다. .claude/agents/ux-designer.md의 지침을 따르세요. 사용자 경험 관점에서 기획안을 검토하고 UI/UX 가이드라인을 제시합니다. TaskList를 확인하고 배정된 작업을 기다리세요."
   - `run_in_background: true`

### 3단계: 팀 구성 완료 안내

팀 생성이 완료되면 사용자에게 다음을 안내합니다:

```
기획팀이 생성되었습니다!

| 역할 | 이름 | 타입 | 권한 |
|------|------|------|------|
| 팀장 | 메인 세션 (나) | - | 전체 (자동) |
| 기획자 | planner | Plan (읽기전용) | 읽기 |
| 리뷰어 | reviewer | Plan (읽기전용) | 읽기 |
| UX담당자 | ux-designer | Plan (읽기전용) | 읽기 |

**분할창 전환**: Shift+Down으로 팀원 간 전환
**태스크 목록**: Ctrl+T로 확인

요구사항을 입력하시면 팀장(메인 세션)이 작업을 분배합니다.
```

## 워크플로우

팀장(메인 세션)은 다음 순서로 작업을 진행합니다:

1. 요구사항 접수 → 기획자에게 분석 요청
2. 기획자의 초안 작성 완료 → UX담당자에게 UX 검토 요청
3. UX 피드백 반영 → 리뷰어에게 최종 검토 요청
4. 리뷰어 승인 → 기획안 확정 및 완료 보고

## 주의사항
- 기획자, 리뷰어, UX담당자 모두 Plan 타입으로 스폰하여 읽기 권한만 부여 (토큰 절약)
- 모든 팀원은 백그라운드로 스폰하여 병렬 실행
- 팀원 이름(name)으로 SendMessage를 통해 소통 가능
- 기획 문서는 local-notes 디렉토리에 작성