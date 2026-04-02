---
name: plan-action
description: plan.md 파일의 구현 계획을 실행합니다. 인자로 날짜+시퀀스 번호(예: 2026040201)를 필수로 전달해야 합니다.
args: plan_id
allowed-tools: Read, Write, Edit, Glob, Grep, Agent, Bash
---

# Plan 실행

## 인자 확인

필수 인자: `$ARGS`

인자가 비어있으면 "plan_id(날짜+시퀀스, 예: 2026040201)를 인자로 전달해주세요. 예: /plan-action 2026040201"라고 안내하고 중단하세요.

## Plan 파일 찾기

!`ls local-notes/$ARGS-*-plan.md 2>/dev/null || echo "NOT_FOUND"`

위 결과가 `NOT_FOUND`이면 "해당 plan 파일을 찾을 수 없습니다: $ARGS" 라고 안내하고 중단하세요.

## 수행 절차

1. 위에서 찾은 plan 파일을 읽습니다.
2. plan 파일의 **4. 상세 구현 방법** 섹션의 Step들을 순서대로 실행합니다.
3. 각 Step을 실행할 때:
   - Step의 내용을 정확히 이해한 후 구현합니다.
   - 기존 코드 패턴과 일관성을 유지합니다.
   - 레거시 코드 참고가 필요한 경우 `legacy/` 디렉토리를 확인합니다.
   - Step 완료 후 다음 Step으로 넘어갑니다.
4. 모든 Step 완료 후 빌드가 정상적으로 되는지 확인합니다.

## 주의사항
- plan 파일에 명시된 범위만 구현하세요. 범위를 벗어나는 추가 작업은 하지 마세요.
- 구현 중 plan과 다르게 진행해야 하는 부분이 있으면 사용자에게 먼저 확인하세요.
- 각 Step의 구현이 끝나면 간단히 완료 상태를 알려주세요.