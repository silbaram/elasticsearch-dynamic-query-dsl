# Tasks: Match 쿼리 단위 테스트 및 리팩토링

**Feature**: 063-match
**Input**: Design documents from `/specs/063-match/`
**Prerequisites**: plan.md, research.md, data-model.md, contracts/match-query-test-contract.md

## Execution Flow (main)
```
1. Load plan.md from feature directory
   → ✅ Implementation plan loaded
   → Tech stack: Kotlin 2.0.20, JDK 21, elasticsearch-java 8.14.3+, Kotest 5.7.1
2. Load optional design documents:
   → data-model.md: MatchQueryDsl, MatchQueryTest entities
   → contracts/: 21 test cases (TC-001 ~ TC-021)
   → research.md: Elasticsearch Match query parameters, test patterns
3. Generate tasks by category:
   → Setup: project verification, dependencies
   → Tests: 21 test cases from contract (TC-018 ~ TC-021 new)
   → Refactoring: Code improvements (KDoc, parameter grouping)
   → Validation: Build, test execution, coverage
4. Apply task rules:
   → Different files = mark [P] for parallel
   → Same file = sequential (no [P])
   → Tests before implementation (TDD)
5. Number tasks sequentially (T001, T002...)
6. Return: SUCCESS (tasks ready for execution)
```

## Format: `[ID] [P?] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- Include exact file paths in descriptions

## Path Conventions
Single project structure:
- Source: `src/main/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/`
- Tests: `src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/`

---

## Phase 3.1: Setup
- [ ] **T001** [P] Verify Gradle build environment and JDK 21 installation
- [ ] **T002** [P] Verify elasticsearch-java 8.14.3+ and Kotest 5.7.1 dependencies in build.gradle.kts
- [ ] **T003** [P] Read existing MatchQueryDsl implementation at `src/main/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/fulltext/MatchQueryDsl.kt`
- [ ] **T004** [P] Read existing MatchQueryTest at `src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/fulltext/MatchQueryTest.kt`
- [ ] **T005** Run existing MatchQueryTest to verify 18 tests pass (TC-001 ~ TC-017, TC-021)

## Phase 3.2: New Tests First (TDD) ⚠️ MUST COMPLETE BEFORE 3.3
**CRITICAL: These tests MUST be written and MUST FAIL before ANY implementation**

### TC-018: Independent Match Query (New)
- [ ] **T006** Write TC-018 test in `src/test/kotlin/.../MatchQueryTest.kt`: "Bool 쿼리 없이 독립 Match 쿼리 생성이 되어야함"
  - Test: `query { matchQuery { field = "title"; query = "elasticsearch" } }`
  - Assert: `query.isMatch shouldBe true`, field="title", query="elasticsearch"

### TC-019: Field Null/Empty Handling (New)
- [ ] **T007** Write TC-019 test in `src/test/kotlin/.../MatchQueryTest.kt`: "field가 null이거나 빈 문자열일 때 Match 쿼리가 생성되지 않아야함"
  - Test null field: `queryOrNull { matchQuery { field = null; query = "test" } }`
  - Test empty field: `queryOrNull { matchQuery { field = ""; query = "test" } }`
  - Assert: Both should be null

### TC-020: Multiple Parameters Combination (New)
- [ ] **T008** Write TC-020 test in `src/test/kotlin/.../MatchQueryTest.kt`: "복수 파라미터를 동시에 설정했을 때 모두 적용되어야함"
  - Test: operator + fuzziness + analyzer + boost + _name together
  - Assert: All parameters correctly applied to match query

### Run Tests to Verify Failures
- [ ] **T009** Run `./gradlew test --tests MatchQueryTest` to verify new tests exist and understand current state
  - Expected: TC-018, TC-019, TC-020 may pass or fail depending on implementation
  - Document: Which tests pass/fail and why

## Phase 3.3: Implementation (if needed)
**Note**: Based on research.md, MatchQueryDsl already implements all required features. This phase verifies implementation completeness.

- [ ] **T010** Verify MatchQueryDsl supports independent match query (without Bool context)
  - Check: Can `query { matchQuery {...} }` work standalone?
  - File: `src/main/kotlin/.../MatchQueryDsl.kt`
  - If failing: Ensure matchQuery extension works on Query.Builder directly

- [ ] **T011** Verify field null/empty handling in matchQuery extension function
  - Check: `val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this`
  - File: `src/main/kotlin/.../MatchQueryDsl.kt`
  - If failing: Ensure early return when field is null/empty

- [ ] **T012** Verify multiple parameters can be combined
  - Check: All optional parameters use `let` for null safety
  - File: `src/main/kotlin/.../MatchQueryDsl.kt`
  - If failing: Ensure all 13 parameters are properly applied

## Phase 3.4: Code Quality & Documentation
**Optional improvements from research.md**

- [ ] **T013** [P] Add KDoc comments to MatchQueryDsl class
  - File: `src/main/kotlin/.../MatchQueryDsl.kt`
  - Document: class purpose, each parameter meaning, usage examples
  - Reference: Elasticsearch Match query documentation

- [ ] **T014** [P] Group MatchQueryDsl parameters by category for readability
  - File: `src/main/kotlin/.../MatchQueryDsl.kt`
  - Groups: Required (field, query), Basic options, Fuzzy matching, Advanced
  - Preserve: All existing functionality and field order

## Phase 3.5: Validation & Testing
- [ ] **T015** Run all MatchQueryTest tests: `./gradlew test --tests MatchQueryTest`
  - Expected: 21+ tests passing (18 existing + 3 new)
  - Verify: TC-001 ~ TC-021 all pass

- [ ] **T016** [P] Run full project test suite: `./gradlew test`
  - Verify: No regressions in other query types (Term, Range, Bool, etc.)
  - Expected: All tests pass

- [ ] **T017** [P] Run project build: `./gradlew build`
  - Verify: Build succeeds without errors
  - Check: JAR artifact created successfully

- [ ] **T018** [P] Verify test coverage for MatchQueryDsl
  - Check: All 13 parameters have test coverage
  - Check: All edge cases covered (null, empty, combinations)
  - Reference: contracts/match-query-test-contract.md

## Phase 3.6: Documentation & Polish
- [ ] **T019** [P] Update CLAUDE.md with Match query testing insights
  - Add: Match query supports 13 parameters with full test coverage
  - Add: Match query can be used independently or in Bool context
  - Keep: Under 150 lines total

- [ ] **T020** Verify quickstart.md examples work correctly
  - File: `/specs/063-match/quickstart.md`
  - Test: TC-018, TC-019, TC-020 example code
  - Verify: All examples run and produce expected results

---

## Dependencies

### Phase Flow
```
Phase 3.1 (Setup)
    ↓
Phase 3.2 (New Tests - TDD)
    ↓
Phase 3.3 (Implementation if needed)
    ↓
Phase 3.4 (Code Quality)
    ↓
Phase 3.5 (Validation)
    ↓
Phase 3.6 (Documentation)
```

### Task Dependencies
- T001-T005 (Setup) → Can run in parallel [P]
- T005 blocks T006-T008 (need to understand existing tests first)
- T006-T008 → Sequential (same file: MatchQueryTest.kt)
- T009 blocks T010-T012 (need test results to guide implementation)
- T010-T012 → Sequential (same file: MatchQueryDsl.kt)
- T013-T014 → Can run in parallel [P] (documentation tasks)
- T015-T018 → Can run in parallel [P] (validation tasks)
- T019-T020 → Can run in parallel [P] (documentation tasks)

---

## Parallel Execution Examples

### Phase 3.1: Setup (All parallel)
```bash
# All setup tasks can run together
./gradlew --version                    # T001
cat build.gradle.kts | grep elasticsearch  # T002
cat src/main/kotlin/.../MatchQueryDsl.kt   # T003
cat src/test/kotlin/.../MatchQueryTest.kt  # T004
```

### Phase 3.2: Test Writing (Sequential - same file)
```kotlin
// T006, T007, T008 must be done sequentially
// All edit the same file: MatchQueryTest.kt
// Cannot parallelize edits to the same file
```

### Phase 3.5: Validation (All parallel)
```bash
# T015-T018 can run in parallel
./gradlew test --tests MatchQueryTest &    # T015
./gradlew test &                           # T016
./gradlew build &                          # T017
wait
# Then check coverage (T018)
```

---

## Test Contract Summary
From `contracts/match-query-test-contract.md`:

### Existing Tests (18 tests - verify these still pass)
- **TC-001 ~ TC-012**: Bool query context tests (must, filter, mustNot, should)
- **TC-013 ~ TC-014**: Common parameters (boost, _name)
- **TC-015 ~ TC-017**: Match-specific parameters (operator, fuzziness, etc.)
- **TC-021**: Multiple match queries in mustQuery DSL

### New Tests (3 tests - add in Phase 3.2)
- **TC-018**: Independent Match query without Bool context
- **TC-019**: Field null/empty value handling
- **TC-020**: Multiple parameters combination

**Total**: 21 test cases

---

## Notes
- **TDD Approach**: Write tests first (T006-T008), verify they make sense (T009), then implement/verify (T010-T012)
- **No Breaking Changes**: All existing 18 tests must continue passing
- **Backwards Compatibility**: Maintain existing API surface
- **File Conflicts**: Tasks modifying same file cannot be marked [P]
- **Test Independence**: Each test case must be independent and repeatable
- **Commit Strategy**: Commit after each phase completion

---

## Validation Checklist
*GATE: All must pass before marking feature complete*

- [ ] All 21 test cases pass (TC-001 ~ TC-021)
- [ ] No regressions in other query types
- [ ] Full build succeeds (`./gradlew build`)
- [ ] Test coverage: MatchQueryDsl class 100%
- [ ] Code quality: KDoc comments added
- [ ] Documentation: quickstart.md examples verified
- [ ] Constitution compliance: All principles upheld

---

## Success Criteria
1. ✅ Minimum 21 test cases implemented and passing
2. ✅ All Elasticsearch Match query parameters tested
3. ✅ Independent Match query support verified
4. ✅ Null/empty value handling verified
5. ✅ Multiple parameter combinations verified
6. ✅ Code quality improved with documentation
7. ✅ No breaking changes to existing API
8. ✅ Full test suite passes
9. ✅ Build succeeds

---

**Task Generation Complete**: 20 tasks organized across 6 phases
**Estimated Time**: 2-4 hours
**Next Step**: Execute tasks sequentially following dependencies
