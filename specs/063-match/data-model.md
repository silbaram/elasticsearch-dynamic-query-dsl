# Data Model: Match 쿼리 단위 테스트 및 리팩토링

**Feature**: 063-match
**Date**: 2025-10-06
**Status**: Design

## 개요

이 문서는 Match 쿼리 단위 테스트 및 리팩토링에 관련된 주요 엔티티(Entity)와 데이터 모델을 정의합니다.

## 엔티티 목록

### 1. MatchQueryDsl
**설명**: Elasticsearch Match 쿼리를 생성하기 위한 Kotlin DSL 클래스

**위치**: `src/main/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/fulltext/MatchQueryDsl.kt`

**필드**:
| 필드명 | 타입 | 필수 | 기본값 | 설명 |
|--------|------|------|--------|------|
| field | String? | ✅ | null | 검색 대상 필드명 (null/빈 값이면 쿼리 생성 안 됨) |
| query | String? | ✅ | null | 검색 텍스트 (null/빈 값이면 쿼리 생성 안 됨) |
| analyzer | String? | ❌ | null | 쿼리 텍스트 분석기 |
| operator | Operator? | ❌ | null | 토큰 결합 방식 (AND/OR) |
| minimumShouldMatch | String? | ❌ | null | 최소 일치 절 수 |
| fuzziness | String? | ❌ | null | 오타 허용도 ("AUTO", "0", "1", "2") |
| prefixLength | Int? | ❌ | null | 퍼지 매칭 시 고정 접두사 길이 |
| maxExpansions | Int? | ❌ | null | 퍼지 매칭 시 최대 확장 수 |
| fuzzyTranspositions | Boolean? | ❌ | null | 인접 문자 전환 허용 |
| fuzzyRewrite | String? | ❌ | null | 퍼지 쿼리 재작성 방법 |
| autoGenerateSynonymsPhraseQuery | Boolean? | ❌ | null | 동의어 phrase 쿼리 자동 생성 |
| lenient | Boolean? | ❌ | null | 타입 불일치 예외 무시 |
| zeroTermsQuery | ZeroTermsQuery? | ❌ | null | 분석기가 모든 토큰 제거 시 동작 (NONE/ALL) |
| boost | Float? | ❌ | null | 관련성 점수 가중치 |
| _name | String? | ❌ | null | 쿼리 이름 (디버깅용) |

**검증 규칙**:
1. `field`가 null이거나 빈 문자열이면 쿼리가 생성되지 않음 (`takeIf { it.isNotBlank() }`)
2. `query`가 null이거나 빈 문자열이면 쿼리가 생성되지 않음 (`takeIf { it.isNotBlank() }`)
3. 모든 optional 필드는 null일 때 Elasticsearch 쿼리에서 생략됨 (`let` 사용)
4. `operator`는 Elasticsearch Java Client의 `Operator` enum 사용 (타입 안전)
5. `zeroTermsQuery`는 Elasticsearch Java Client의 `ZeroTermsQuery` enum 사용 (타입 안전)

**관계**:
- `Query.Builder.matchQuery(fn: MatchQueryDsl.() -> Unit)` 확장 함수를 통해 사용됨
- Bool 쿼리의 must, filter, mustNot, should 컨텍스트에서 호출 가능
- 독립적으로도 사용 가능 (`query { matchQuery { ... } }`)

**상태 전이**: 없음 (Immutable DSL)

---

### 2. MatchQueryTest
**설명**: MatchQueryDsl의 동작을 검증하는 Kotest 테스트 클래스

**위치**: `src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/fulltext/MatchQueryTest.kt`

**테스트 카테고리**:

#### A. Bool 쿼리 컨텍스트 테스트 (12개)
- must 쿼리에서 match 쿼리 생성 (3개)
- filter 쿼리에서 match 쿼리 생성 (3개)
- mustNot 쿼리에서 match 쿼리 생성 (3개)
- should 쿼리에서 match 쿼리 생성 (3개)

#### B. 공통 파라미터 테스트 (2개)
- boost 설정 테스트
- _name 설정 테스트

#### C. Match 쿼리 특화 파라미터 테스트 (3개)
- operator, minimum_should_match, analyzer 테스트
- zero_terms_query, lenient, auto_generate_synonyms_phrase_query 테스트
- fuzziness 관련 옵션 테스트

#### D. 추가 엣지 케이스 테스트 (3개 이상)
- 독립 Match 쿼리 생성 테스트 (Bool 쿼리 없이)
- field가 null/빈 값일 때 처리 테스트
- 복수 파라미터 조합 테스트

**검증 방법**:
```kotlin
// 1. Bool 쿼리 타입 검증
query.isBool shouldBe true

// 2. 생성된 쿼리 개수 검증
mustQuery.size shouldBe N

// 3. Match 쿼리 필터링 및 필드 검증
mustQuery.filter { it.isMatch }.find { it.match().field() == "a" }

// 4. 파라미터 값 검증
matchQuery.match().query().stringValue() shouldBe "expected"
matchQuery.match().operator() shouldBe Operator.And
matchQuery.match().boost() shouldBe 2.0F
```

**테스트 데이터**:
- 필드명: "a", "b", "c", "title", "body", "content", "name"
- 검색 텍스트: "1111", "2222", "kotlin", "dsl", "elasticsearch", "quick brown fox"
- 파라미터 조합: 실제 Elasticsearch에서 유효한 값 사용

---

## 엔티티 관계도

```
┌─────────────────────────────────────┐
│      MatchQueryDsl (DSL 클래스)     │
│                                     │
│  - field: String?                   │
│  - query: String?                   │
│  - analyzer: String?                │
│  - operator: Operator?              │
│  - minimumShouldMatch: String?      │
│  - fuzziness: String?               │
│  - prefixLength: Int?               │
│  - maxExpansions: Int?              │
│  - fuzzyTranspositions: Boolean?    │
│  - fuzzyRewrite: String?            │
│  - autoGenerateSynonymsPhraseQuery  │
│  - lenient: Boolean?                │
│  - zeroTermsQuery: ZeroTermsQuery?  │
│  - boost: Float?                    │
│  - _name: String?                   │
└────────────┬────────────────────────┘
             │
             │ 사용됨
             │
             ▼
┌─────────────────────────────────────┐
│  Query.Builder.matchQuery()        │
│  (확장 함수)                        │
│                                     │
│  1. DSL 적용                        │
│  2. null/빈 값 검증                 │
│  3. Elasticsearch Query 생성        │
└────────────┬────────────────────────┘
             │
             │ 생성
             │
             ▼
┌─────────────────────────────────────┐
│  Elasticsearch MatchQuery          │
│  (elasticsearch-java 클라이언트)    │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│      MatchQueryTest (테스트)        │
│                                     │
│  A. Bool 컨텍스트 테스트 (12개)    │
│  B. 공통 파라미터 테스트 (2개)     │
│  C. 특화 파라미터 테스트 (3개)     │
│  D. 엣지 케이스 테스트 (3개)       │
│                                     │
│  총 20개 이상                       │
└────────────┬────────────────────────┘
             │
             │ 검증
             │
             ▼
┌─────────────────────────────────────┐
│      MatchQueryDsl                  │
│      (검증 대상)                     │
└─────────────────────────────────────┘
```

## 참조 엔티티

### TermQueryDsl (참조 구조)
**위치**: `src/main/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/termlevel/TermLevelDsl.kt`

**유사점**:
- DSL 클래스 + 확장 함수 패턴
- null/빈 값 자동 제거 로직 (`takeIf { it.isNotBlank() }`)
- optional 파라미터는 `let` 사용
- boost, _name 공통 파라미터

**차이점**:
- TermQuery: `value` 필드 (String)
- MatchQuery: `query` 필드 (String) + 추가 파라미터 13개

### RangeQueryDsl (참조 구조)
**위치**: `src/main/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/termlevel/TermLevelDsl.kt`

**유사점**:
- DSL 클래스 + 확장 함수 패턴
- 복수 optional 파라미터 처리

**차이점**:
- RangeQuery: from/to, gt/lt/gte/lte 파라미터
- MatchQuery: query + fuzziness/operator/analyzer 등 파라미터

## 데이터 흐름

```
1. 사용자 코드
   ↓
2. matchQuery { field = "title"; query = "elasticsearch" }
   ↓
3. MatchQueryDsl 인스턴스 생성 및 초기화
   ↓
4. Query.Builder.matchQuery() 확장 함수 호출
   ↓
5. null/빈 값 검증 (field, query)
   ↓
6. Elasticsearch Java Client MatchQuery.Builder 호출
   ↓
7. optional 파라미터 적용 (let 사용)
   ↓
8. Elasticsearch Query 객체 생성
   ↓
9. Elasticsearch로 전송 (JSON 직렬화)
```

## 검증 요구사항

### 기능적 검증
1. ✅ 필수 파라미터(field, query) 누락 시 쿼리 생성 안 됨
2. ✅ null/빈 값 자동 제거 동작
3. ✅ 모든 optional 파라미터 정상 적용
4. ✅ Bool 쿼리 컨텍스트(must, filter, mustNot, should) 모두 지원
5. ✅ 독립 Match 쿼리 생성 가능
6. ✅ 복수 파라미터 조합 시 모두 적용됨

### 비기능적 검증
1. ✅ 타입 안정성: Operator, ZeroTermsQuery enum 사용
2. ✅ 자동 정제: null 값은 Elasticsearch JSON에서 자동 생략
3. ✅ 성능: DSL 호출 O(1) 복잡도
4. ✅ 호환성: 기존 API 유지 (하위 호환성)

---
**작성일**: 2025-10-06
**다음 단계**: contracts/ 생성
