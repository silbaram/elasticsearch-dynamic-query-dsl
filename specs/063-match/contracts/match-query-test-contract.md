# Test Contract: Match Query

**Feature**: 063-match
**Contract Type**: Unit Test
**Date**: 2025-10-06

## 개요

이 계약은 MatchQueryDsl이 만족해야 하는 테스트 요구사항을 정의합니다. 모든 테스트는 실패한 후 (Red), 구현/리팩토링을 통해 통과해야 합니다 (Green).

## 테스트 계약

### A. Bool 쿼리 컨텍스트 테스트 (기존 12개 + 검증)

#### A1. must 쿼리 컨텍스트
**TC-001**: must 쿼리에서 match 쿼리 생성이 되어야함
**Given**: Bool 쿼리 + must 컨텍스트
**When**: matchQuery { field = "a"; query = "1111" } 호출
**Then**: must 절에 Match 쿼리가 생성되고, field="a", query="1111" 값이 설정됨

**TC-002**: must 쿼리에서 matchQuery에 query 값이 비었거나 null면 제외가 되어야함
**Given**: Bool 쿼리 + must 컨텍스트
**When**: matchQuery { field = "a"; query = null } 호출
**Then**: Match 쿼리가 생성되지 않음

**TC-003**: must 쿼리에서 matchQuery가 없을때 must쿼리는 생성 안되야함
**Given**: Bool 쿼리 + must 컨텍스트
**When**: 모든 matchQuery의 query가 null/빈 값
**Then**: must 절이 빈 배열로 생성됨 (크기 0)

#### A2. filter 쿼리 컨텍스트
**TC-004**: filter 쿼리에서 match 쿼리 생성이 되어야함
**TC-005**: filter 쿼리에서 matchQuery에 query 값이 비었거나 null면 제외가 되어야함
**TC-006**: filter 쿼리에서 matchQuery가 없을때 filter쿼리는 생성 안되야함

#### A3. mustNot 쿼리 컨텍스트
**TC-007**: mustNot 쿼리에서 match 쿼리 생성이 되어야함
**TC-008**: mustNot 쿼리에서 matchQuery에 query 값이 비었거나 null면 제외가 되어야함
**TC-009**: mustNot 쿼리에서 matchQuery가 없을때 mustNot쿼리는 생성 안되야함

#### A4. should 쿼리 컨텍스트
**TC-010**: should 쿼리에서 match 쿼리 생성이 되어야함
**TC-011**: should 쿼리에서 matchQuery에 query 값이 비었거나 null면 제외가 되어야함
**TC-012**: should 쿼리에서 matchQuery가 없을때 should쿼리는 생성 안되야함

---

### B. 공통 파라미터 테스트 (기존 2개 + 검증)

**TC-013**: match 쿼리에 boost 설정시 적용이 되어야함
**Given**: matchQuery { field = "a"; query = "1111"; boost = 2.0F }
**Then**: match.boost() == 2.0F

**TC-014**: match 쿼리에 _name이 설정되면 match.queryName에 반영되어야함
**Given**: matchQuery { field = "a"; query = "1111"; _name = "named" }
**Then**: match.queryName() == "named"

---

### C. Match 쿼리 특화 파라미터 테스트 (기존 3개 + 검증)

**TC-015**: match 쿼리에서 operator, minimum_should_match, analyzer 설정이 적용되어야함
**Given**: matchQuery {
  field = "title"
  query = "quick brown fox"
  operator = Operator.And
  minimumShouldMatch = "2"
  analyzer = "standard"
}
**Then**:
- match.operator() == Operator.And
- match.minimumShouldMatch() == "2"
- match.analyzer() == "standard"

**TC-016**: match 쿼리에서 zero_terms_query, lenient, auto_generate_synonyms_phrase_query 설정이 적용되어야함
**Given**: matchQuery {
  field = "content"
  query = "the"
  zeroTermsQuery = ZeroTermsQuery.All
  lenient = true
  autoGenerateSynonymsPhraseQuery = true
}
**Then**:
- match.zeroTermsQuery() == ZeroTermsQuery.All
- match.lenient() == true
- match.autoGenerateSynonymsPhraseQuery() == true

**TC-017**: match 쿼리에서 fuzziness 관련 옵션 설정이 적용되어야함
**Given**: matchQuery {
  field = "name"
  query = "jon"
  fuzziness = "AUTO"
  prefixLength = 1
  maxExpansions = 50
  fuzzyTranspositions = true
  fuzzyRewrite = "constant_score"
}
**Then**:
- match.fuzziness() == "AUTO"
- match.prefixLength() == 1
- match.maxExpansions() == 50
- match.fuzzyTranspositions() == true
- match.fuzzyRewrite() == "constant_score"

---

### D. 엣지 케이스 테스트 (신규 3개 추가)

**TC-018** (신규): 독립 Match 쿼리 생성 테스트
**Given**: Bool 쿼리 없이 Match 쿼리만 사용
**When**: query { matchQuery { field = "title"; query = "elasticsearch" } }
**Then**:
- query.isMatch shouldBe true
- match.field() == "title"
- match.query().stringValue() == "elasticsearch"

**TC-019** (신규): field가 null/빈 값일 때 처리 테스트
**Given**: matchQuery { field = null; query = "test" } 또는 matchQuery { field = ""; query = "test" }
**Then**: Match 쿼리가 생성되지 않음 (쿼리 빌더에서 early return)

**TC-020** (신규): 복수 파라미터 조합 테스트
**Given**: matchQuery {
  field = "content"
  query = "elasticsearch search"
  operator = Operator.And
  fuzziness = "AUTO"
  analyzer = "standard"
  boost = 1.5F
  _name = "complex_match"
}
**Then**:
- 모든 파라미터가 정상적으로 적용됨
- match.operator() == Operator.And
- match.fuzziness() == "AUTO"
- match.analyzer() == "standard"
- match.boost() == 1.5F
- match.queryName() == "complex_match"

**TC-021** (신규): mustQuery DSL은 queries 블록 없이도 여러 match 쿼리를 추가할 수 있어야 함
**Given**: boolQuery { mustQuery { matchQuery {...}; matchQuery {...} } }
**Then**: must 절에 2개의 Match 쿼리가 생성됨

---

## 테스트 실행 기준

### Red-Green-Refactor 사이클
1. **Red**: 테스트 작성 → 실행 → 실패 확인
2. **Green**: 최소한의 코드로 테스트 통과
3. **Refactor**: 코드 개선 (구조, 가독성) → 테스트 여전히 통과

### 테스트 통과 기준
- 모든 테스트가 통과해야 함 (`./gradlew test`)
- 테스트 커버리지: MatchQueryDsl 클래스 100%
- 테스트 실행 시간: 전체 5초 이내
- 테스트 독립성: 각 테스트는 다른 테스트에 영향을 주지 않음

### 실패 시 처리
- TC-001 ~ TC-017: 기존 테스트이므로 이미 통과함 (검증만 필요)
- TC-018 ~ TC-021: 신규 테스트이므로 추가 작성 필요
- 테스트 실패 시 원인 분석 → 코드 수정 → 재실행

---

## 계약 검증 방법

### 자동 검증 (CI)
```bash
# 모든 테스트 실행
./gradlew test

# Match Query 테스트만 실행
./gradlew test --tests MatchQueryTest

# 테스트 리포트 확인
cat build/reports/tests/test/index.html
```

### 수동 검증
1. 각 테스트 케이스를 Kotest로 작성
2. `./gradlew test` 실행
3. 모든 테스트가 PASS인지 확인
4. 실패한 테스트가 있으면 원인 분석 및 수정

---

## 계약 위반 시 조치

| 위반 사항 | 조치 |
|-----------|------|
| 테스트 케이스 누락 | 누락된 테스트 추가 작성 |
| 테스트 실패 | 코드 수정 또는 테스트 수정 (요구사항 재확인) |
| 테스트 실행 시간 초과 | 테스트 로직 최적화 |
| 테스트 간 의존성 발견 | 테스트 독립성 확보 (setup/teardown) |

---

## 테스트 데이터 표준

### 필드명
- 단순 테스트: "a", "b", "c"
- 의미 있는 테스트: "title", "content", "body", "name", "description"

### 검색 텍스트
- 숫자: "1111", "2222", "3333"
- 단어: "kotlin", "dsl", "elasticsearch", "search", "test"
- 구문: "quick brown fox", "elasticsearch search"

### 파라미터 값
- operator: Operator.And, Operator.Or
- fuzziness: "AUTO", "0", "1", "2"
- analyzer: "standard", "english", "keyword"
- zeroTermsQuery: ZeroTermsQuery.None, ZeroTermsQuery.All
- boost: 1.0F, 1.5F, 2.0F, 3.0F
- _name: "named", "my_match_query", "complex_match"

---

**계약 작성일**: 2025-10-06
**계약 승인자**: Feature specification from spec.md
**다음 단계**: 테스트 케이스 구현 (TC-018 ~ TC-021)
