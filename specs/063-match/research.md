# Research: Match 쿼리 단위 테스트 및 리팩토링

**Feature**: 063-match
**Date**: 2025-10-06
**Status**: Complete

## 조사 개요

이 문서는 Match 쿼리의 포괄적인 단위 테스트 추가 및 리팩토링을 위한 기술 조사 결과를 정리합니다.

## 1. Elasticsearch Match 쿼리 파라미터

### Decision
Elasticsearch 8.x Match 쿼리의 전체 파라미터 목록을 기반으로 테스트 케이스를 작성합니다.

### Rationale
- 현재 MatchQueryDsl 클래스는 대부분의 파라미터를 구현하고 있음
- 그러나 MatchQueryTest에는 모든 파라미터에 대한 검증이 부족함
- 포괄적인 테스트 커버리지를 위해 모든 파라미터의 동작을 검증해야 함

### Elasticsearch Match 쿼리 파라미터 전체 목록

#### 필수 파라미터
1. **query**: string | number | date | boolean - 검색 텍스트/값

#### 선택적 파라미터 (현재 MatchQueryDsl에 구현됨)
1. **operator**: Operator (기본값: OR) - "AND" 또는 "OR"
2. **analyzer**: String - 쿼리 텍스트 분석기
3. **fuzziness**: String (예: "AUTO", "0", "1", "2") - 오타 허용도
4. **prefix_length**: Int (기본값: 0) - 퍼지 매칭 시 고정 접두사 길이
5. **max_expansions**: Int (기본값: 50) - 퍼지 매칭 시 최대 확장 수
6. **fuzzy_transpositions**: Boolean (기본값: true) - 인접 문자 전환 허용
7. **fuzzy_rewrite**: String - 퍼지 쿼리 재작성 방법
8. **auto_generate_synonyms_phrase_query**: Boolean (기본값: true) - 동의어 phrase 쿼리 자동 생성
9. **lenient**: Boolean (기본값: false) - 타입 불일치 예외 무시
10. **zero_terms_query**: ZeroTermsQuery ("none" 또는 "all") - 분석기가 모든 토큰 제거 시 동작
11. **minimum_should_match**: String | Integer - 최소 일치 절 수
12. **boost**: Float (기본값: 1.0) - 관련성 점수 가중치
13. **_name**: String - 쿼리 이름 (디버깅용)

### Alternatives Considered
1. **기본 파라미터만 테스트**: query, operator, fuzziness만 테스트
   - 기각 이유: 헌법 IV (완전한 Elasticsearch 커버리지) 위반
2. **일부 파라미터만 테스트**: 자주 사용되는 5-7개 파라미터만 테스트
   - 기각 이유: 사용자가 모든 파라미터를 사용할 수 있어야 라이브러리의 가치가 극대화됨

## 2. 기존 테스트 구조 분석

### Decision
프로젝트 내 다른 쿼리 타입(Term, Range)의 테스트 구조를 참조하여 Match 쿼리 테스트를 확장합니다.

### Rationale
- 프로젝트 내 일관된 테스트 패턴 유지
- 기존 테스트 구조는 Bool 쿼리 컨텍스트(must, filter, mustNot, should)별로 조직화되어 있음
- 기존 테스트는 null/빈 값 처리, boost, _name 등을 체계적으로 검증

### 기존 테스트 패턴 (TermQueryTest, RangeQueryTest)

#### 1. Bool 쿼리 컨텍스트별 테스트
- **must 쿼리에서 생성 테스트**
- **filter 쿼리에서 생성 테스트**
- **mustNot 쿼리에서 생성 테스트**
- **should 쿼리에서 생성 테스트**

#### 2. Null/빈 값 처리 테스트
- 각 Bool 컨텍스트별로 null/빈 값 자동 제거 검증
- 모든 쿼리가 제거될 때 상위 쿼리도 생성되지 않음을 검증

#### 3. 공통 파라미터 테스트
- **boost 테스트**: 관련성 점수 가중치 설정 검증
- **_name 테스트**: 쿼리 이름 설정 및 queryName() 반영 검증

#### 4. 쿼리별 특화 파라미터 테스트
- RangeQueryTest: from/to, gt/lt, gte/lte 테스트
- TermQueryTest: value 검증 테스트

### 현재 MatchQueryTest 분석

#### 기존 테스트 커버리지 (13개 테스트)
1. ✅ must 쿼리에서 match 쿼리 생성
2. ✅ mustQuery DSL은 queries 블록 없이도 여러 match 쿼리 추가 가능
3. ✅ must 쿼리에서 query 값이 null/빈 값이면 제외
4. ✅ must 쿼리에서 matchQuery가 없을때 must쿼리는 생성 안됨
5. ✅ filter 쿼리에서 match 쿼리 생성
6. ✅ filter 쿼리에서 query 값이 null/빈 값이면 제외
7. ✅ filter 쿼리에서 matchQuery가 없을때 filter쿼리는 생성 안됨
8. ✅ mustNot 쿼리에서 match 쿼리 생성
9. ✅ mustNot 쿼리에서 query 값이 null/빈 값이면 제외
10. ✅ mustNot 쿼리에서 matchQuery가 없을때 mustNot쿼리는 생성 안됨
11. ✅ should 쿼리에서 match 쿼리 생성
12. ✅ should 쿼리에서 query 값이 null/빈 값이면 제외
13. ✅ should 쿼리에서 matchQuery가 없을때 should쿼리는 생성 안됨
14. ✅ match 쿼리에 boost 설정시 적용
15. ✅ match 쿼리에 _name 설정시 적용
16. ✅ operator, minimum_should_match, analyzer 설정 적용
17. ✅ zero_terms_query, lenient, auto_generate_synonyms_phrase_query 설정 적용
18. ✅ fuzziness 관련 옵션 설정 적용

**총 18개의 기존 테스트 존재** - 기본적인 테스트 구조는 완성되어 있음!

#### 부족한 테스트 커버리지
1. ❌ 독립 Match 쿼리 생성 테스트 (Bool 쿼리 없이)
2. ❌ 파라미터 조합 테스트 (복수 파라미터 동시 설정)
3. ❌ 엣지 케이스 테스트
   - null field 테스트
   - 빈 field 테스트
   - 유효하지 않은 fuzziness 값 테스트
4. ❌ 파라미터별 세부 동작 테스트
   - operator: AND vs OR 동작 차이
   - fuzziness: AUTO vs 숫자 값 차이
   - zero_terms_query: none vs all 동작 차이

### Alternatives Considered
1. **완전히 새로운 테스트 구조 도입**
   - 기각 이유: 프로젝트 내 일관성 저해
2. **기존 테스트 전부 재작성**
   - 기각 이유: 기존 테스트는 이미 잘 작성되어 있으며, 추가만 필요함

## 3. MatchQueryDsl 구조 분석

### Decision
MatchQueryDsl은 TermLevelDsl의 구조 패턴과 일관성을 유지합니다.

### Rationale
- 현재 MatchQueryDsl과 TermQueryDsl은 이미 유사한 구조를 가지고 있음
- 두 구조 모두 null/빈 값 자동 제거 로직을 사용함
- 두 구조 모두 Elasticsearch API를 래핑하는 DSL 클래스 + 확장 함수 패턴

### 구조 비교

#### 공통 패턴
```kotlin
// 1. DSL 클래스 정의
class XxxQueryDsl {
    var field: String? = null
    var value/query: String? = null  // 주요 값
    var boost: Float? = null
    var _name: String? = null
    // 쿼리별 특화 파라미터들
}

// 2. 확장 함수 (Query.Builder에 추가)
fun Query.Builder.xxxQuery(fn: XxxQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = XxxQueryDsl().apply(fn)
    // null/빈 값 검증
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val v = dsl.value?.takeIf { it.isNotBlank() } ?: return this

    // Elasticsearch API 호출
    return this.xxx { x ->
        x.field(f)
        x.value(v)
        // optional 파라미터들
        dsl.boost?.let { x.boost(it) }
        dsl._name?.let { x.queryName(it) }
        x
    }
}
```

#### MatchQueryDsl 현재 구조
```kotlin
class MatchQueryDsl {
    var field: String? = null
    var query: String? = null
    var analyzer: String? = null
    var operator: Operator? = null
    var minimumShouldMatch: String? = null
    var fuzziness: String? = null
    var prefixLength: Int? = null
    var maxExpansions: Int? = null
    var fuzzyTranspositions: Boolean? = null
    var fuzzyRewrite: String? = null
    var autoGenerateSynonymsPhraseQuery: Boolean? = null
    var lenient: Boolean? = null
    var zeroTermsQuery: ZeroTermsQuery? = null
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.matchQuery(fn: MatchQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = MatchQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val q = dsl.query?.takeIf { it.isNotBlank() } ?: return this

    return this.match { m ->
        m.field(f)
        m.query(q)
        dsl.analyzer?.let { m.analyzer(it) }
        dsl.operator?.let { m.operator(it) }
        // ... 나머지 optional 파라미터들
        m
    }
}
```

### 리팩토링 포인트
1. ✅ 구조는 이미 일관적임 - 추가 리팩토링 불필요
2. ✅ null/빈 값 자동 제거 로직 존재
3. ✅ 모든 optional 파라미터는 let을 사용하여 null일 때 생략됨
4. ⚠️ 가능한 개선: 코드 주석 추가 (KDoc)
5. ⚠️ 가능한 개선: 파라미터 그룹화 (예: fuzziness 관련 파라미터들을 함께 배치)

### Alternatives Considered
1. **Builder 패턴으로 리팩토링**
   - 기각 이유: 현재 DSL 패턴이 Kotlin다우며, 프로젝트 전체에 일관되게 사용됨
2. **타입 안전 enum 클래스 도입 (operator, fuzziness 등)**
   - 기각 이유: Elasticsearch Java 클라이언트가 이미 타입 안전 클래스 제공 (Operator, ZeroTermsQuery)
   - fuzziness는 문자열("AUTO", "0", "1", "2")이므로 String 타입 유지가 적절함

## 4. Kotest FunSpec 테스트 작성 베스트 프랙티스

### Decision
Kotest FunSpec 스타일을 유지하며, 테스트 이름은 한글로 작성하여 가독성을 높입니다.

### Rationale
- 프로젝트 전체가 Kotest FunSpec을 사용하고 있음
- 테스트 이름은 한글로 작성하여 "무엇을 테스트하는지" 명확히 표현
- shouldBe, shouldNotBe 등 Kotest의 fluent assertion 사용

### 테스트 작성 패턴
```kotlin
class MatchQueryTest : FunSpec({
    test("테스트 설명") {
        // Given: 초기 상태 설정
        val query = query {
            boolQuery {
                mustQuery { matchQuery { field = "a"; query = "1111" } }
            }
        }

        // When: 테스트 대상 실행
        val mustQuery = query.bool().must()

        // Then: 결과 검증
        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.first().match().field() shouldBe "a"
    }
})
```

### Best Practices
1. **테스트 이름 작성**
   - "무엇을" + "어떤 조건에서" + "어떻게 동작해야 하는지" 명확히 표현
   - 예: "must 쿼리에서 match 쿼리 생성이 되어야함"

2. **Assertion 작성**
   - shouldBe, shouldNotBe 사용
   - null 체크는 shouldBe null 또는 shouldNotBe null
   - 컬렉션 크기는 .size shouldBe N
   - 특정 요소 찾기는 .find { ... } 또는 .filter { ... }

3. **테스트 데이터**
   - 의미 있는 테스트 값 사용 (예: "elasticsearch", "search engine")
   - 필요시 숫자/문자 혼합 (예: "1111", "2222")

4. **테스트 조직화**
   - Bool 쿼리 컨텍스트별로 그룹화 (must, filter, mustNot, should)
   - 공통 파라미터 테스트 (boost, _name)
   - 특화 파라미터 테스트 (operator, fuzziness 등)

### Alternatives Considered
1. **영어로 테스트 이름 작성**
   - 기각 이유: 프로젝트 전체가 한글 테스트 이름 사용
2. **Given-When-Then 주석 추가**
   - 선택 사항: 복잡한 테스트에만 추가

## 5. 추가 테스트 케이스 계획

### Decision
기존 18개 테스트에 추가로 최소 3개 이상의 테스트 케이스를 추가하여 총 21개 이상의 테스트를 확보합니다.

### 추가 필요 테스트 목록

#### 엣지 케이스 테스트 (3개 이상)
1. **독립 Match 쿼리 생성 테스트**
   - Bool 쿼리 없이 Match 쿼리만 단독으로 생성 가능한지 검증

2. **field가 null/빈 값일 때 처리 테스트**
   - field가 null이거나 빈 문자열일 때 쿼리가 생성되지 않음을 검증

3. **복수 파라미터 조합 테스트**
   - operator + fuzziness + analyzer를 동시에 설정했을 때 모두 적용되는지 검증

### Rationale
- 기존 18개 테스트가 이미 대부분의 시나리오를 커버하고 있음
- 추가 3개만으로 21개가 되어 "최소 15개 이상" 요구사항을 충족함
- 엣지 케이스와 조합 테스트를 통해 포괄적인 커버리지 달성

### Alternatives Considered
1. **30개 이상의 테스트 추가**
   - 기각 이유: 과도한 테스트는 유지보수 비용 증가, 현재 18개로 충분히 포괄적
2. **모든 파라미터 조합 테스트**
   - 기각 이유: 조합 폭발 (13개 파라미터의 조합은 수천 개), 비효율적

## 결론

### 주요 결정 사항
1. **Elasticsearch Match 쿼리의 13개 파라미터를 모두 테스트**
2. **기존 테스트 구조 유지 및 확장**: 18개 → 21개 이상
3. **MatchQueryDsl 구조는 현재 상태 유지** (이미 일관적이며 잘 작성됨)
4. **Kotest FunSpec + 한글 테스트 이름 패턴 유지**
5. **TDD 원칙 준수**: 테스트 추가 → 실패 확인 → (필요시) 리팩토링 → 테스트 통과

### 다음 단계 (Phase 1)
1. data-model.md 작성 (MatchQueryDsl, MatchQueryTest 엔티티 정의)
2. contracts/ 작성 (테스트 계약 정의)
3. quickstart.md 작성 (개발자를 위한 빠른 시작 가이드)
4. AGENTS.md 업데이트 (AI 에이전트용 컨텍스트 추가)

---
**조사 완료일**: 2025-10-06
**다음 Phase**: Phase 1 - Design & Contracts
