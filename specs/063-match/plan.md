
# Implementation Plan: Match 쿼리 단위 테스트 및 리팩토링

**Branch**: `063-match` | **Date**: 2025-10-06 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/063-match/spec.md`

## Execution Flow (/plan command scope)
```
1. Load feature spec from Input path
   → ✅ Feature spec loaded successfully
2. Fill Technical Context (scan for NEEDS CLARIFICATION)
   → ✅ Project Type detected: Single (Kotlin library)
   → ✅ Structure Decision: Gradle-based Kotlin library with src/main and src/test
3. Fill the Constitution Check section based on the content of the constitution document.
   → ✅ Constitution loaded and evaluated
4. Evaluate Constitution Check section below
   → ✅ No violations detected - TDD approach aligns with constitution
   → Update Progress Tracking: Initial Constitution Check
5. Execute Phase 0 → research.md
   → In progress
6. Execute Phase 1 → contracts, data-model.md, quickstart.md, AGENTS.md
   → Pending
7. Re-evaluate Constitution Check section
   → Pending
8. Plan Phase 2 → Describe task generation approach (DO NOT create tasks.md)
   → Pending
9. STOP - Ready for /tasks command
```

## Summary
이 기능은 기존 MatchQueryTest 파일에 포괄적인 단위 테스트 케이스(최소 15개)를 추가하고, Match 쿼리 구현체를 프로젝트 내 다른 쿼리 타입(Term, Range 등)과 일관된 구조로 리팩토링합니다. 기존 API는 유지하여 하위 호환성을 보장하며, 모든 Elasticsearch Match 쿼리 파라미터(query, operator, fuzziness, analyzer, minimum_should_match, zero_terms_query 등)를 검증합니다.

## Technical Context
**Language/Version**: Kotlin 2.0.20, JDK 21
**Primary Dependencies**: elasticsearch-java 8.14.3+, Kotest 5.7.1, kotlinx-coroutines-core 1.7.3
**Storage**: N/A (라이브러리 프로젝트)
**Testing**: Kotest FunSpec, JUnit 5
**Target Platform**: JVM 21+, Elasticsearch 8.x 호환
**Project Type**: Single (Gradle-based Kotlin library)
**Performance Goals**: DSL 빌더 호출 O(1) 복잡도 유지
**Constraints**: 기존 API 하위 호환성 유지, null/빈 값 자동 정제
**Scale/Scope**: 최소 15개 이상의 포괄적 테스트 케이스, Match 쿼리 구현체 리팩토링

## Constitution Check
*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ I. 타입 안정성 우선
- **현재 상태**: Match 쿼리는 타입 안전한 DSL로 구현되어 있음 (MatchQueryDsl 클래스)
- **이번 작업**: 리팩토링 시 타입 안정성 유지, 테스트에서 타입 안정성 검증
- **준수 여부**: ✅ PASS - 기존 타입 안전 구조 유지

### ✅ II. 자동 정제
- **현재 상태**: Match 쿼리는 null/빈 값 자동 제거 로직 구현됨 (`takeIf { it.isNotBlank() }`)
- **이번 작업**: 리팩토링 시 자동 정제 로직 유지 및 개선
- **준수 여부**: ✅ PASS - 기존 자동 정제 메커니즘 유지

### ✅ III. 테스트 주도 개발 (필수)
- **현재 상태**: MatchQueryTest 파일 존재하나 테스트 케이스가 부족함
- **이번 작업**: TDD 원칙에 따라 테스트 우선 작성 → 리팩토링 수행
- **준수 여부**: ✅ PASS - 테스트 우선 접근 방식 채택

### ✅ IV. 완전한 Elasticsearch 커버리지
- **현재 상태**: Match 쿼리 기본 파라미터는 구현되어 있으나 일부 파라미터 미검증
- **이번 작업**: 모든 Elasticsearch Match 쿼리 파라미터 검증 테스트 추가
- **준수 여부**: ✅ PASS - 완전한 파라미터 커버리지 목표

### ✅ V. 라이브러리 우선
- **현재 상태**: 독립적인 라이브러리로 설계됨
- **이번 작업**: 기존 API 유지 (하위 호환성), 내부 구현만 개선
- **준수 여부**: ✅ PASS - 라이브러리 안정성 유지

**초기 Constitution Check 결과**: ✅ PASS - 모든 헌법 원칙 준수

## Project Structure

### Documentation (this feature)
```
specs/063-match/
├── spec.md              # Feature specification (완료)
├── plan.md              # This file (/plan command output)
├── research.md          # Phase 0 output (/plan command)
├── data-model.md        # Phase 1 output (/plan command)
├── quickstart.md        # Phase 1 output (/plan command)
├── contracts/           # Phase 1 output (/plan command)
└── tasks.md             # Phase 2 output (/tasks command - NOT created by /plan)
```

### Source Code (repository root)
```
src/
├── main/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/
│   ├── queries/
│   │   ├── fulltext/
│   │   │   └── MatchQueryDsl.kt          # 리팩토링 대상
│   │   └── termlevel/
│   │       └── TermLevelDsl.kt           # 참조 구조 (Term, Range)
│   └── core/
│       └── QueryDsl.kt
│
└── test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/
    └── queries/
        ├── fulltext/
        │   └── MatchQueryTest.kt         # 테스트 추가 대상
        └── termlevel/
            ├── TermQueryTest.kt          # 참조 테스트 구조
            └── RangeQueryTest.kt         # 참조 테스트 구조
```

**Structure Decision**: Single project (Gradle-based Kotlin library).
- 소스 코드: `src/main/kotlin/` (구현체)
- 테스트 코드: `src/test/kotlin/` (Kotest 테스트)
- 참조 쿼리: Term, Range 쿼리 구조를 리팩토링 참조 기준으로 사용

## Phase 0: Outline & Research
1. **Extract unknowns from Technical Context**:
   - ✅ 모든 기술 컨텍스트 명확화 완료
   - Elasticsearch Match 쿼리 공식 문서 파라미터 리스트 확인 필요
   - 프로젝트 내 다른 쿼리 타입의 구조 패턴 분석 필요

2. **Generate and dispatch research agents**:
   ```
   Task 1: "Elasticsearch 8.x Match 쿼리의 모든 파라미터 및 동작 방식 조사"
   Task 2: "TermQueryTest, RangeQueryTest의 테스트 패턴 및 구조 분석"
   Task 3: "MatchQueryDsl과 TermLevelDsl의 구조 비교 및 리팩토링 포인트 도출"
   Task 4: "Kotest FunSpec을 사용한 포괄적 테스트 케이스 작성 베스트 프랙티스"
   ```

3. **Consolidate findings** in `research.md` using format:
   - Decision: Elasticsearch Match 쿼리 파라미터 전체 목록
   - Rationale: 포괄적 테스트 커버리지를 위해 필요
   - Alternatives considered: 기본 파라미터만 테스트 vs 전체 파라미터 테스트

**Output**: research.md with all technical decisions documented

## Phase 1: Design & Contracts
*Prerequisites: research.md complete*

1. **Extract entities from feature spec** → `data-model.md`:
   - Entity: MatchQueryDsl (DSL 클래스)
   - Entity: MatchQueryTest (테스트 클래스)
   - 필드: query, operator, fuzziness, analyzer, minimumShouldMatch, zeroTermsQuery 등
   - 검증 규칙: null/빈 값 자동 제거, 모든 파라미터 optional

2. **Generate API contracts** from functional requirements:
   - N/A (라이브러리 프로젝트 - REST API 없음)
   - 대신 테스트 계약(Test Contract) 정의:
     * 각 Match 쿼리 파라미터별 테스트 시나리오
     * 엣지 케이스 테스트 시나리오
     * 참조 쿼리(Term, Range)와의 구조 일관성 테스트

3. **Generate contract tests** from contracts:
   - 최소 15개 이상의 테스트 케이스 정의
   - 각 파라미터별 테스트 (query, operator, fuzziness 등)
   - 엣지 케이스 테스트 (null, 빈 값, 유효하지 않은 값)
   - Bool 쿼리 컨텍스트별 테스트 (must, filter, mustNot, should)

4. **Extract test scenarios** from user stories:
   - Scenario 1: 기본 Match 쿼리 생성 및 검증
   - Scenario 2: 모든 파라미터 조합 테스트
   - Scenario 3: null/빈 값 자동 제거 검증
   - Scenario 4: 다른 쿼리 타입과 구조 일관성 검증
   - Scenario 5: 리팩토링 후 기존 테스트 통과 확인

5. **Update agent file incrementally** (O(1) operation):
   - Run `.specify/scripts/bash/update-agent-context.sh claude`
   - Add: Match 쿼리 리팩토링 컨텍스트
   - Add: Elasticsearch Match 쿼리 파라미터 정보
   - Preserve: 기존 프로젝트 컨텍스트
   - Keep under 150 lines for token efficiency

**Output**: data-model.md, /contracts/*, failing tests, quickstart.md, AGENTS.md

## Phase 2: Task Planning Approach
*This section describes what the /tasks command will do - DO NOT execute during /plan*

**Task Generation Strategy**:
1. **Setup tasks**: Gradle 환경 확인, 의존성 확인
2. **Research tasks** [P]: Elasticsearch Match 쿼리 파라미터 조사
3. **Test planning tasks** [P]: 15개 이상의 테스트 케이스 설계
4. **Test implementation tasks**: TDD 순서로 테스트 작성
   - 기본 Match 쿼리 테스트 (T001-T003)
   - 파라미터별 테스트 (T004-T012)
   - 엣지 케이스 테스트 (T013-T015)
   - Bool 쿼리 컨텍스트별 테스트 (T016-T019)
5. **Refactoring tasks**: Match 쿼리 구현체 리팩토링 (테스트 통과 후)
6. **Validation tasks**: 전체 테스트 실행 및 검증

**Ordering Strategy**:
- TDD order: 테스트 작성 → 실패 확인 → 리팩토링 → 테스트 통과
- 파일별 순서:
  * [P] Research tasks (병렬 가능)
  * [P] Test case design tasks (병렬 가능)
  * Sequential: Test implementation (MatchQueryTest.kt)
  * Sequential: Refactoring (MatchQueryDsl.kt)
  * [P] Validation (병렬 가능)

**Estimated Output**: 25-30 numbered, ordered tasks in tasks.md

**IMPORTANT**: This phase is executed by the /tasks command, NOT by /plan

## Phase 3+: Future Implementation
*These phases are beyond the scope of the /plan command*

**Phase 3**: Task execution (/tasks command creates tasks.md)
**Phase 4**: Implementation (execute tasks.md following constitutional principles)
**Phase 5**: Validation (run tests, execute quickstart.md, performance validation)

## Complexity Tracking
*Fill ONLY if Constitution Check has violations that must be justified*

**해당 없음** - 모든 헌법 원칙을 준수하며, 추가적인 복잡성 증가 없음.

## Progress Tracking
*This checklist is updated during execution flow*

**Phase Status**:
- [x] Phase 0: Research complete (/plan command) - research.md 생성 완료
- [x] Phase 1: Design complete (/plan command) - data-model.md, contracts/, quickstart.md, CLAUDE.md 생성 완료
- [x] Phase 2: Task planning complete (/plan command - describe approach only) - Phase 2 섹션 작성 완료
- [ ] Phase 3: Tasks generated (/tasks command) - 다음 단계
- [ ] Phase 4: Implementation complete
- [ ] Phase 5: Validation passed

**Gate Status**:
- [x] Initial Constitution Check: PASS - 모든 헌법 원칙 준수
- [x] Post-Design Constitution Check: PASS - 설계 후 재검토 완료, 헌법 위반 사항 없음
- [x] All NEEDS CLARIFICATION resolved - 모든 불명확 사항 해결
- [x] Complexity deviations documented (N/A) - 복잡성 증가 없음

**Artifacts Generated**:
- ✅ `/specs/063-match/research.md` - Elasticsearch Match 쿼리 파라미터 조사 및 기존 코드 분석
- ✅ `/specs/063-match/data-model.md` - MatchQueryDsl, MatchQueryTest 엔티티 정의
- ✅ `/specs/063-match/contracts/match-query-test-contract.md` - 21개 테스트 케이스 계약
- ✅ `/specs/063-match/quickstart.md` - 개발자를 위한 빠른 시작 가이드
- ✅ `/CLAUDE.md` - Claude Code용 컨텍스트 파일 업데이트

---
*Based on Constitution v1.0.0 - See `.specify/memory/constitution.md`*
