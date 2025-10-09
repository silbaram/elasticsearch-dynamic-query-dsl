# Tasks: 전이 의존성 문제 해결

**Feature**: 064-transitive-dependency-fix
**Input**: Design documents from `/specs/064-transitive-dependency-fix/`
**Prerequisites**: ✅ plan.md, ✅ research.md, ✅ data-model.md, ✅ contracts/

## Execution Flow (this file)
```
1. Load plan.md from feature directory
   → ✅ Tech stack: Kotlin 2.0.20, JDK 21, Gradle 8.10.2
   → ✅ Structure: Single Gradle-based Kotlin library
2. Load optional design documents:
   → ✅ data-model.md: GradlePublishingConfiguration, MavenPomDependency, TestConsumerProject
   → ✅ contracts/: POM verification, Consumer build success
   → ✅ research.md: 3-step solution approach
   → ✅ quickstart.md: 6-step validation guide
3. Generate tasks by category:
   → Setup: Gradle 환경 확인, 기존 테스트 실행
   → Tests: POM 검증 스크립트, 테스트 소비자 프로젝트
   → Core: build.gradle.kts 수정 (pom.withXml 추가)
   → Integration: publishToMavenLocal, POM 검증, 소비자 프로젝트 빌드
   → Polish: 문서 업데이트, 전체 회귀 테스트
4. Apply task rules:
   → 테스트 우선 (TDD 원칙)
   → build.gradle.kts 수정은 순차 실행
   → 검증 스크립트와 소비자 프로젝트는 병렬 가능
5. Number tasks sequentially (T001-T020)
6. Validate: 모든 계약에 대한 검증 작업 포함
```

## Format: `[ID] [P?] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- All file paths are absolute

## Phase 3.1: Setup
- [ ] **T001** - Verify Gradle environment and existing tests pass
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/`
  - **Command**: `./gradlew clean test`
  - **Expected**: All existing tests pass (회귀 방지 확인)
  - **Blocks**: Nothing (must pass before proceeding)

- [ ] **T002** - Verify current build.gradle.kts configuration
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/build.gradle.kts`
  - **Action**: Read and confirm `java-library` plugin and `api` dependencies exist
  - **Expected**:
    * `java-library` plugin applied
    * `elasticsearch-java` in `api` configuration
    * `kotlinx-coroutines-core` in `api` configuration
  - **Blocks**: T003

## Phase 3.2: Tests First (TDD) ⚠️ MUST COMPLETE BEFORE 3.3
**CRITICAL: These verification scripts MUST be created and MUST FAIL before implementation**

- [ ] **T003** [P] - Create POM verification script
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/verify-pom.sh`
  - **Purpose**: Bash script to verify POM file dependencies per `contracts/pom-verification-contract.md`
  - **Content**:
    ```bash
    #!/bin/bash
    POM_FILE=~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom

    # Check file exists
    # Check elasticsearch-java dependency exists
    # Check kotlinx-coroutines-core dependency exists
    # Check scope is compile (or empty)
    ```
  - **Expected**: Script fails initially (POM may have missing dependencies)
  - **Blocks**: T007

- [ ] **T004** [P] - Create test-consumer project structure
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/test-consumer/`
  - **Action**: Create directory structure per `data-model.md` TestConsumerProject entity
  - **Files to create**:
    * `test-consumer/settings.gradle.kts`
    * `test-consumer/build.gradle.kts`
    * `test-consumer/src/test/kotlin/test/consumer/` (directory)
  - **Blocks**: T005

- [ ] **T005** - Write test-consumer build.gradle.kts
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/test-consumer/build.gradle.kts`
  - **Purpose**: Single dependency configuration per `contracts/consumer-build-contract.md`
  - **Content**:
    ```kotlin
    plugins {
        kotlin("jvm") version "2.0.20"
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        testImplementation("io.github.silbaram:elasticsearch-dynamic-query-dsl:1.0.0-SNAPSHOT")
        testImplementation("io.kotest:kotest-runner-junit5:5.7.1")
        testImplementation("io.kotest:kotest-assertions-core:5.7.1")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    kotlin {
        jvmToolchain(21)
    }
    ```
  - **Expected**: File created with exact single dependency
  - **Blocks**: T006

- [ ] **T006** - Write ConsumerTest.kt with failing tests
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/test-consumer/src/test/kotlin/test/consumer/ConsumerTest.kt`
  - **Purpose**: Test Query.Builder access and DSL functionality per `contracts/consumer-build-contract.md`
  - **Content**: Complete ConsumerTest from contract (4 test cases)
    * Query.Builder 접근 가능
    * Bool 쿼리 생성
    * Match 쿼리 생성
    * 코루틴 API 사용
  - **Expected**: Tests fail to compile initially (전이 의존성 누락)
  - **Blocks**: T012

- [ ] **T007** - Run publishToMavenLocal and verify POM fails
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/`
  - **Command**: `./gradlew publishToMavenLocal && ./verify-pom.sh`
  - **Expected**:
    * publishToMavenLocal succeeds
    * verify-pom.sh fails (dependencies missing or wrong scope)
  - **Purpose**: Establish baseline - confirms problem exists
  - **Blocks**: T008

## Phase 3.3: Core Implementation (ONLY after tests are failing)

- [ ] **T008** - Add pom.withXml block to build.gradle.kts
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/build.gradle.kts`
  - **Action**: Modify publishing.publications.mavenJava.pom block
  - **Add**: `withXml` block that explicitly adds dependencies per `research.md` section 5
  - **Code to add**:
    ```kotlin
    withXml {
        val dependenciesNode = asNode().appendNode("dependencies")

        // api dependencies → compile scope
        configurations.named("api").get().allDependencies.forEach { dep ->
            if (dep.group != null && dep.name != null) {
                val dependencyNode = dependenciesNode.appendNode("dependency")
                dependencyNode.appendNode("groupId", dep.group)
                dependencyNode.appendNode("artifactId", dep.name)
                dependencyNode.appendNode("version", dep.version)
                dependencyNode.appendNode("scope", "compile")
            }
        }

        // implementation dependencies → runtime scope
        configurations.named("implementation").get().allDependencies.forEach { dep ->
            if (dep.group != null && dep.name != null) {
                val dependencyNode = dependenciesNode.appendNode("dependency")
                dependencyNode.appendNode("groupId", dep.group)
                dependencyNode.appendNode("artifactId", dep.name)
                dependencyNode.appendNode("version", dep.version)
                dependencyNode.appendNode("scope", "runtime")
            }
        }
    }
    ```
  - **Expected**: build.gradle.kts modified with explicit dependency mapping
  - **Blocks**: T009

## Phase 3.4: Integration & Verification

- [ ] **T009** - Republish to mavenLocal with new configuration
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/`
  - **Command**: `./gradlew clean publishToMavenLocal`
  - **Expected**: BUILD SUCCESSFUL
  - **Blocks**: T010

- [ ] **T010** - Verify POM file now passes verification
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/`
  - **Command**: `./verify-pom.sh`
  - **Expected**:
    * ✅ POM file exists
    * ✅ elasticsearch-java dependency found with compile scope
    * ✅ kotlinx-coroutines-core dependency found with compile scope
  - **Purpose**: Contract `pom-verification-contract.md` satisfied
  - **Blocks**: T011

- [ ] **T011** - Inspect generated POM file manually
  - **Path**: `~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom`
  - **Command**: `cat [POM_FILE] | grep -A 50 "<dependencies>"`
  - **Expected**: Visual confirmation of XML structure per `data-model.md`
  - **Blocks**: T012

- [ ] **T012** - Copy Gradle wrapper to test-consumer
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/test-consumer/`
  - **Command**: `cp -r ../gradle ./ && cp ../gradlew ./ && cp ../gradlew.bat ./`
  - **Expected**: test-consumer can run Gradle commands independently
  - **Blocks**: T013

- [ ] **T013** - Run test-consumer build and verify compilation success
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/test-consumer/`
  - **Command**: `./gradlew clean build`
  - **Expected**:
    * Compilation succeeds (no "Cannot access class 'Query.Builder'" error)
    * Tests pass
  - **Purpose**: Contract `consumer-build-contract.md` satisfied
  - **Blocks**: T014

- [ ] **T014** - Verify Query.Builder accessibility
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/test-consumer/`
  - **Command**: `./gradlew test --tests ConsumerTest`
  - **Expected**: All 4 test cases pass
    * Query.Builder 접근 가능
    * Bool 쿼리 생성
    * Match 쿼리 생성
    * 코루틴 API 사용
  - **Purpose**: End-to-end validation of transitive dependency fix
  - **Blocks**: T015

## Phase 3.5: Regression & Polish

- [ ] **T015** - Run main project tests to ensure no regression
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/`
  - **Command**: `./gradlew clean test`
  - **Expected**: All existing tests still pass (기존 21개 MatchQuery 테스트 포함)
  - **Purpose**: Verify build.gradle.kts changes don't break existing functionality
  - **Blocks**: T016

- [ ] **T016** [P] - Verify dependency tree in test-consumer
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/test-consumer/`
  - **Command**: `./gradlew dependencies --configuration testRuntimeClasspath | grep elasticsearch-java`
  - **Expected**:
    * elasticsearch-java appears exactly once
    * No version conflicts
  - **Blocks**: Nothing

- [ ] **T017** [P] - Verify Gradle module metadata (.module file)
  - **Path**: `~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/`
  - **Command**: `cat elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.module | jq '.variants'`
  - **Expected**:
    * apiElements variant includes elasticsearch-java and kotlinx-coroutines-core
    * runtimeElements variant includes all dependencies
  - **Purpose**: Verify Gradle metadata per `data-model.md` section
  - **Blocks**: Nothing

- [ ] **T018** - Update constitution compliance notes
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/.specify/memory/constitution.md`
  - **Action**: Add note documenting successful resolution of transitive dependency issue
  - **Expected**: Constitution v1.1.0 "V. 라이브러리 우선 - 전이 의존성 관리" now fully implemented
  - **Blocks**: T019

- [ ] **T019** - Clean up verification script (optional)
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/verify-pom.sh`
  - **Action**: Either commit for CI/CD use or remove if one-time validation
  - **Decision**: Keep if useful for future releases, remove if not needed
  - **Blocks**: T020

- [ ] **T020** - Final validation checklist
  - **Path**: `/Users/qoo10/projects/elasticsearch-dynamic-query-dsl/specs/064-transitive-dependency-fix/`
  - **Action**: Verify all success criteria from `spec.md` are met:
    * ✅ 로컬 테스트 성공 (publishToMavenLocal → test-consumer 사용)
    * ✅ Query.Builder 접근 가능
    * ✅ POM 검증 통과 (elasticsearch-java, kotlinx-coroutines-core in compile scope)
    * ✅ 기존 API 호환성 유지 (모든 테스트 통과)
  - **Expected**: All checkboxes can be marked ✅
  - **Blocks**: Nothing (feature complete)

## Dependencies
```
T001 (setup) → T002 (verify config)
T002 → T003, T004 (parallel test setup)
T003 → T007 (baseline verification)
T004 → T005 → T006 (consumer test creation)
T007 → T008 (confirms problem exists before fix)
T008 (implementation) → T009 (republish)
T009 → T010 → T011 (POM verification chain)
T011 → T012 → T013 → T014 (consumer validation chain)
T014 → T015 (regression check)
T015 → T016, T017, T018, T019 (parallel polish)
T016, T017, T018, T019 → T020 (final validation)
```

## Parallel Execution Examples

### Phase 3.2: Create test artifacts in parallel
```bash
# T003 and T004 can run simultaneously (different directories)
# However, T003 is just a script creation, so minimal parallelization benefit
```

### Phase 3.5: Polish tasks in parallel
```bash
# After T015 passes, run these simultaneously:
cd /Users/qoo10/projects/elasticsearch-dynamic-query-dsl/test-consumer
./gradlew dependencies --configuration testRuntimeClasspath | grep elasticsearch-java &

cd /Users/qoo10/projects/elasticsearch-dynamic-query-dsl
cat ~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.module | jq '.variants' &
```

## Task Generation Rules Applied
1. ✅ **From Contracts**:
   - `pom-verification-contract.md` → T003 (verification script), T010 (contract validation)
   - `consumer-build-contract.md` → T004-T006 (test project setup), T013-T014 (contract validation)

2. ✅ **From Data Model**:
   - `GradlePublishingConfiguration` → T008 (pom.withXml implementation)
   - `MavenPomDependency` → T010-T011 (POM structure verification)
   - `TestConsumerProject` → T004-T006 (project creation), T012-T014 (build & test)

3. ✅ **From Quickstart**:
   - Step 1-2 → T001-T002 (setup)
   - Step 3 → T007, T010-T011 (POM verification)
   - Step 4 → T004-T006, T012-T014 (test consumer)
   - Step 5 → T008 (implementation)
   - Step 6 → T015-T020 (validation)

4. ✅ **Ordering**:
   - Setup (T001-T002) → Tests (T003-T007) → Implementation (T008) → Verification (T009-T014) → Polish (T015-T020)
   - TDD enforced: T007 confirms problem before T008 implements fix

## Validation Checklist
*GATE: All items must be checked before marking feature complete*

- [x] All contracts have corresponding validation tasks
  - `pom-verification-contract.md` → T003, T010
  - `consumer-build-contract.md` → T013, T014
- [x] All entities have implementation tasks
  - `GradlePublishingConfiguration` → T008
  - `TestConsumerProject` → T004-T006
- [x] All tests come before implementation
  - T003-T007 (tests/verification) before T008 (implementation)
- [x] Parallel tasks are truly independent
  - T003, T004 (different directories)
  - T016, T017 (read-only checks)
- [x] Each task specifies exact file path ✅
- [x] No task modifies same file as another [P] task ✅

## Notes
- **Critical Path**: T001 → T002 → T007 → T008 → T009 → T010 → T013 → T014 → T015
- **Estimated Time**: ~45 minutes total (per quickstart.md)
- **Risk**: T007 may pass if dependencies already correct (unlikely based on spec.md problem description)
- **Constitution**: All principles maintained (타입 안정성, 자동 정제, TDD, ES 커버리지, 라이브러리 우선)
- **Scope**: Build configuration only - no source code changes

---
**Generated**: 2025-10-08
**Ready for**: `/implement` command or manual execution
**Status**: ✅ All 20 tasks defined and ordered
