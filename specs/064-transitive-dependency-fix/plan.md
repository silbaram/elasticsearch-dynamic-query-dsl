
# Implementation Plan: 전이 의존성 문제 해결

**Branch**: `064-transitive-dependency-fix` | **Date**: 2025-10-08 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/064-transitive-dependency-fix/spec.md`

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
   → ✅ No violations detected - transitive dependency fix aligns with constitution
   → Update Progress Tracking: Initial Constitution Check
5. Execute Phase 0 → research.md
   → In progress
6. Execute Phase 1 → contracts, data-model.md, quickstart.md, CLAUDE.md
   → Pending
7. Re-evaluate Constitution Check section
   → Pending
8. Plan Phase 2 → Describe task generation approach (DO NOT create tasks.md)
   → Pending
9. STOP - Ready for /tasks command
```

## Summary
이 기능은 배포된 라이브러리를 다른 프로젝트에서 사용할 때 발생하는 "Cannot access class 'Query.Builder'" 에러를 해결합니다. 문제의 근본 원인은 Gradle의 `java-library` 플러그인과 Maven publishing 설정에서 `api` 의존성이 제대로 전파되지 않는 것입니다. 해결 방안으로 POM 파일에 의존성을 명시적으로 설정하고, Gradle 메타데이터가 올바르게 생성되도록 보장합니다.

## Technical Context
**Language/Version**: Kotlin 2.0.20, JDK 21
**Primary Dependencies**: elasticsearch-java 8.14.3+, kotlinx-coroutines-core 1.7.3, Gradle 8.10.2
**Storage**: N/A (라이브러리 프로젝트)
**Testing**: Kotest 5.7.1, JUnit 5, 로컬 Maven 저장소 테스트
**Target Platform**: JVM 21+, Gradle/Maven 빌드 시스템
**Project Type**: Single (Gradle-based Kotlin library)
**Performance Goals**: 빌드 시간 최소화, 의존성 해결 시간 < 5초
**Constraints**: 기존 API 하위 호환성 유지, Maven Central/GitHub Packages 호환
**Scale/Scope**: 단일 라이브러리 배포 설정 수정, 별도 테스트 프로젝트 생성

## Constitution Check
*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ I. 타입 안정성 우선
- **현재 상태**: 이 작업은 빌드 설정 수정이며 타입 시스템에 영향 없음
- **이번 작업**: Gradle 및 Maven publishing 설정만 수정
- **준수 여부**: ✅ PASS - 소스 코드 변경 없음

### ✅ II. 자동 정제
- **현재 상태**: DSL의 자동 정제 로직은 유지됨
- **이번 작업**: 빌드 설정 수정으로 자동 정제에 영향 없음
- **준수 여부**: ✅ PASS - 기존 로직 유지

### ✅ III. 테스트 주도 개발 (필수)
- **현재 상태**: 기존 테스트 모두 유지
- **이번 작업**: 로컬 Maven 저장소 테스트로 검증
- **준수 여부**: ✅ PASS - 통합 테스트로 검증

### ✅ IV. 완전한 Elasticsearch 커버리지
- **현재 상태**: 모든 쿼리/집계 기능 유지
- **이번 작업**: API 변경 없음
- **준수 여부**: ✅ PASS - 기능 변경 없음

### ✅ V. 라이브러리 우선
- **현재 상태**: 전이 의존성 관리 문제 발생
- **이번 작업**: **헌법 v1.1.0의 전이 의존성 관리 요구사항 이행**
  * POM 파일에서 `elasticsearch-java`가 `compile` 스코프로 명시
  * `kotlinx-coroutines-core`가 `runtime` 스코프로 명시
  * Gradle 메타데이터 검증
- **준수 여부**: ✅ PASS - 헌법 요구사항 직접 이행

**초기 Constitution Check 결과**: ✅ PASS - 모든 헌법 원칙 준수

## Project Structure

### Documentation (this feature)
```
specs/064-transitive-dependency-fix/
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
# 기존 구조 유지 (변경 없음)
src/
├── main/kotlin/
│   └── com/github/silbaram/elasticsearch/dynamic_query_dsl/
└── test/kotlin/
    └── com/github/silbaram/elasticsearch/dynamic_query_dsl/

# 빌드 설정 파일 (수정 대상)
build.gradle.kts        # Publishing 설정 수정
gradle.properties       # 버전 정보

# 검증용 테스트 프로젝트 (신규 생성)
test-consumer/
├── build.gradle.kts    # 라이브러리를 의존성으로 추가
└── src/test/kotlin/    # 간단한 사용 테스트
```

**Structure Decision**: Single project (Gradle-based Kotlin library). 소스 코드 변경 없이 빌드 설정만 수정하며, 별도 테스트 프로젝트를 생성하여 의존성 해결을 검증합니다.

## Phase 0: Outline & Research
1. **Extract unknowns from Technical Context** above:
   - ✅ 모든 기술 컨텍스트 명확화 완료
   - Gradle `java-library` 플러그인의 `api` vs `implementation` 동작 방식
   - Maven POM 파일 생성 시 의존성 스코프 매핑 규칙
   - Gradle 모듈 메타데이터 vs Maven POM 우선순위
   - `publishToMavenLocal`로 생성된 POM 검증 방법

2. **Generate and dispatch research agents**:
   ```
   Task 1: "Gradle java-library 플러그인에서 api 의존성이 POM 파일로 변환되는 과정 조사"
   Task 2: "Maven POM의 compile/runtime 스코프와 Gradle api/implementation 매핑 규칙 조사"
   Task 3: "Gradle 모듈 메타데이터(.module 파일)와 Maven POM의 우선순위 및 호환성 조사"
   Task 4: "publishToMavenLocal 후 생성된 POM 파일 위치 및 검증 방법 조사"
   ```

3. **Consolidate findings** in `research.md` using format:
   - Decision: POM 파일에 의존성을 명시적으로 추가
   - Rationale: `java-library` 플러그인만으로는 불충분할 수 있음
   - Alternatives considered: Gradle 메타데이터만 의존 vs POM 명시

**Output**: research.md with all technical decisions documented

## Phase 1: Design & Contracts
*Prerequisites: research.md complete*

1. **Extract entities from feature spec** → `data-model.md`:
   - Entity: GradlePublishingConfiguration (빌드 설정)
   - Entity: MavenPomDependency (POM 의존성)
   - Entity: TestConsumerProject (검증 프로젝트)
   - 필드: dependency scope (compile/runtime), transitivity (true/false)
   - 검증 규칙: POM에 api 의존성이 compile 스코프로 존재

2. **Generate API contracts** from functional requirements:
   - N/A (라이브러리 내부 API 변경 없음)
   - 대신 빌드 계약(Build Contract) 정의:
     * 생성된 POM 파일에 elasticsearch-java가 compile 스코프로 포함
     * 생성된 POM 파일에 kotlinx-coroutines-core가 runtime 스코프로 포함
     * 테스트 프로젝트에서 단일 의존성만으로 컴파일 성공

3. **Generate contract tests** from contracts:
   - POM 파일 검증 테스트 (XML 파싱)
   - 테스트 프로젝트 빌드 성공 테스트
   - Query.Builder 접근 가능 테스트

4. **Extract test scenarios** from user stories:
   - Scenario 1: 로컬 Maven 저장소에 배포 후 다른 프로젝트에서 사용
   - Scenario 2: POM 파일에 의존성이 올바르게 포함되는지 검증
   - Scenario 3: 기존 테스트가 모두 통과하는지 확인 (회귀 방지)

5. **Update agent file incrementally** (O(1) operation):
   - Run `.specify/scripts/bash/update-agent-context.sh claude`
   - Add: Gradle publishing 설정, POM 의존성 관리
   - Preserve: 기존 DSL 패턴 및 아키텍처 설명
   - Keep under 150 lines for token efficiency

**Output**: data-model.md, /contracts/*, test project, quickstart.md, CLAUDE.md

## Phase 2: Task Planning Approach
*This section describes what the /tasks command will do - DO NOT execute during /plan*

**Task Generation Strategy**:
1. **Setup tasks**: Gradle 환경 확인, 기존 테스트 실행
2. **Research tasks** [P]: Gradle/Maven 문서 조사 (4개 task)
3. **Build configuration tasks**: build.gradle.kts 수정
   - POM dependencies 블록 추가
   - withVariantsFromConfiguration 설정
4. **Verification tasks**:
   - publishToMavenLocal 실행
   - POM 파일 검증 (XML 파싱)
5. **Test project tasks**:
   - test-consumer 프로젝트 생성
   - build.gradle.kts 작성
   - 간단한 DSL 사용 테스트 작성
   - 빌드 성공 확인
6. **Validation tasks**: 전체 테스트 실행, 회귀 방지

**Ordering Strategy**:
- Setup → Research (parallel) → Build config → Verification → Test project → Validation
- Different files = mark [P] for parallel
- Same file (build.gradle.kts) = sequential

**Estimated Output**: 15-20 numbered, ordered tasks in tasks.md

**IMPORTANT**: This phase is executed by the /tasks command, NOT by /plan

## Phase 3+: Future Implementation
*These phases are beyond the scope of the /plan command*

**Phase 3**: Task execution (/tasks command creates tasks.md)
**Phase 4**: Implementation (execute tasks.md following constitutional principles)
**Phase 5**: Validation (run tests, verify POM, test-consumer project success)

## Complexity Tracking
*Fill ONLY if Constitution Check has violations that must be justified*

**해당 없음** - 모든 헌법 원칙을 준수하며, 빌드 설정만 수정하므로 복잡성 증가 없음.

## Progress Tracking
*This checklist is updated during execution flow*

**Phase Status**:
- [x] Phase 0: Research complete (/plan command) - research.md 완료
- [x] Phase 1: Design complete (/plan command) - data-model.md, contracts/, quickstart.md, CLAUDE.md 완료
- [x] Phase 2: Task planning complete (/plan command - describe approach only) - Phase 2 섹션 작성 완료
- [ ] Phase 3: Tasks generated (/tasks command) - 다음 단계
- [ ] Phase 4: Implementation complete
- [ ] Phase 5: Validation passed

**Gate Status**:
- [x] Initial Constitution Check: PASS - 모든 헌법 원칙 준수
- [x] Post-Design Constitution Check: PASS - 빌드 설정만 수정, 소스 코드 변경 없음
- [x] All NEEDS CLARIFICATION resolved - 모든 기술 컨텍스트 명확함
- [x] Complexity deviations documented (N/A) - 복잡성 증가 없음

**Artifacts Generated**:
- ✅ `/specs/064-transitive-dependency-fix/spec.md` - Feature specification 완료
- ✅ `/specs/064-transitive-dependency-fix/research.md` - Phase 0 완료
- ✅ `/specs/064-transitive-dependency-fix/data-model.md` - Phase 1 완료
- ✅ `/specs/064-transitive-dependency-fix/contracts/pom-verification-contract.md` - Phase 1 완료
- ✅ `/specs/064-transitive-dependency-fix/contracts/consumer-build-contract.md` - Phase 1 완료
- ✅ `/specs/064-transitive-dependency-fix/quickstart.md` - Phase 1 완료
- ✅ `/CLAUDE.md` - Phase 1에서 업데이트 완료

---
*Based on Constitution v1.1.0 - See `.specify/memory/constitution.md`*
