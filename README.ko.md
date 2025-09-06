# Kotlin용 Elasticsearch 동적 쿼리 DSL

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

코틀린 타입‑세이프 빌더로 Elasticsearch 쿼리를 동적으로 조합하는 DSL입니다. null/빈 입력은 자동 생략되어 출력 JSON을 간결하고 유효하게 유지합니다.

영문 문서: README.md

## 주요 특징
- 직관적 DSL: JSON 대신 코틀린 빌더 사용
- 동적 생략: null/빈 값은 자동 제외
- 전문 쿼리: match, match_phrase, match_bool_prefix, multi_match(type=phrase), combined_fields
- 점수 조작: field value factor, script score, weight, random score, decay
- Kotlin/JDK 17, Kotest + JUnit 5

## 빠른 시작
- 빌드/테스트: `./gradlew clean build`
- 로컬 배포: `./gradlew publishToMavenLocal`

최소 예제
```kotlin
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.*

val q: Query = query {
  boolQuery {
    mustQuery {
      queries[
        matchPhraseQuery("message", "this is a test"),
        matchPhrasePrefixQuery("path", "/api/ad"),
        multiMatchPhraseQuery("quick brown fox", listOf("title^2", "body")),
        combinedFields(query = "john smith", fields = listOf("first_name", "last_name"))
      ]
    }
  }
}
```

## 간단 사용법
- Bool + 절 구성
```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.*

val q = query {
  boolQuery {
    mustQuery { termQuery("user.id", "silbaram") }
    filterQuery { rangeQuery("age", gte = 20, lt = 30) }
    shouldQuery { queries[ termQuery("tags", "kotlin"), termQuery("tags", "elasticsearch") ] }
    mustNotQuery { existsQuery("deleted_at") }
  }
}
```

- 전문 검색 (한 줄 예시)
```kotlin
matchPhraseQuery("title", "exact order", slop = 1)
matchBoolPrefixQuery(field = "title", query = "quick bro")
multiMatchPhraseQuery("kotlin coroutine", listOf("title^2", "description"))
queryStringQuery("kotlin* AND \"structured query\"", listOf("title","body"))
simpleQueryStringQuery("kotlin +coroutine | \"structured query\"", listOf("title","body"))
```

간단 JSON
```json
{ "query": { "bool": { "must": [{ "term": { "user.id": "silbaram" }}] } } }
{ "query": { "match_phrase": { "title": { "query": "exact order", "slop": 1 } } } }
{ "query": { "combined_fields": { "query": "john smith", "fields": ["first_name","last_name"], "operator": "and", "minimum_should_match": "2" } } }
```

### Multi‑match (일반형)
`multiMatchQuery` 또는 `Query.Builder.multiMatch`를 사용합니다.

```kotlin
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
import co.elastic.clients.elasticsearch._types.query_dsl.Operator

multiMatchQuery(
  query = "kotlin coroutine",
  fields = listOf("title^2", "description"),
  type = TextQueryType.BestFields,
  operator = Operator.Or,
  minimumShouldMatch = "2"
)
```

JSON
```json
{ "query": { "multi_match": {
  "query": "kotlin coroutine",
  "fields": ["title^2", "description"],
  "type": "best_fields",
  "operator": "or",
  "minimum_should_match": "2"
} } }
```

테스트: [MultiMatchQueryTest.kt](src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/fulltext/MultiMatchQueryTest.kt)

### Query string
Lucene 질의 문법으로 여러 필드에 질의를 적용합니다.

```kotlin
import co.elastic.clients.elasticsearch._types.query_dsl.Operator

queryStringQuery(
  query = "title:(kotlin AND coroutine) AND body:tips",
  fields = listOf("title^2","body"),
  defaultOperator = Operator.And
)
```

JSON
```json
{ "query": { "query_string": {
  "query": "title:(kotlin AND coroutine) AND body:tips",
  "fields": ["title^2","body"],
  "default_operator": "and"
} } }
```

테스트: [QueryStringQueryTest.kt](src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/fulltext/QueryStringQueryTest.kt)

### Simple query string
파싱 오류를 던지지 않는 관대한 문법으로, 지원되지 않는 구문은 무시됩니다.

```kotlin
simpleQueryStringQuery(
  query = "kotlin +coroutine | \"structured query\"",
  fields = listOf("title^2","body"),
  // 자주 쓰는 옵션
  defaultOperator = Operator.Or,
  minimumShouldMatch = "2",
  analyzeWildcard = true,
  flags = listOf(SimpleQueryStringFlag.Prefix, SimpleQueryStringFlag.Phrase),
  fuzzyMaxExpansions = 50,
  fuzzyPrefixLength = 1,
  fuzzyTranspositions = true
)
```

JSON
```json
{ "query": { "simple_query_string": {
  "query": "kotlin +coroutine | \"structured query\"",
  "fields": ["title^2","body"],
  "default_operator": "or",
  "minimum_should_match": "2",
  "analyze_wildcard": true,
  "flags": "PREFIX|PHRASE",
  "fuzzy_max_expansions": 50,
  "fuzzy_prefix_length": 1,
  "fuzzy_transpositions": true
} } }
```

테스트: [SimpleQueryStringQueryTest.kt](src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/fulltext/SimpleQueryStringQueryTest.kt)

- Combined fields
```kotlin
import co.elastic.clients.elasticsearch._types.query_dsl.CombinedFieldsOperator

combinedFields(
  query = "john smith",
  fields = listOf("first_name", "last_name"),
  operator = CombinedFieldsOperator.And,
  minimumShouldMatch = "2"
)
```

메모: `text` 필드를 사용하세요. null/빈 입력은 쿼리에서 생략됩니다.

## Function Score
함수별 필터, field value factor, weight, random, decay 등을 조합하세요.
테스트:
- 기본: [FunctionScoreTest.kt](src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/compound/FunctionScoreTest.kt)
- Kibana 유사: [FunctionScoreKibanaParityTest.kt](src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/compound/FunctionScoreKibanaParityTest.kt)
- Decay: [DecayFunctionTest.kt](src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/compound/DecayFunctionTest.kt)

## 프로젝트 구조
- `src/main/kotlin`: DSL 및 쿼리 빌더
- `src/test/kotlin`: Kotest 스펙(JUnit 5)
- Gradle Kotlin DSL, JDK 17 툴체인

## 라이선스
Apache License 2.0 — LICENSE 참조.

## 배포
- 빌드/테스트: `./gradlew clean build`
- 로컬 배포: `./gradlew publishToMavenLocal`
의존성 예시
```kotlin
repositories { mavenLocal(); mavenCentral() }
dependencies { implementation("com.github.silbaram:elasticsearch-dynamic-query-dsl:1.0-SNAPSHOT") }
```

## 고급 옵션 요약

Combined fields (combined_fields)

| 옵션 | 타입 | 비고 |
|---|---|---|
| `fields` | List<String> | 대상 필드(`^`로 가중치) |
| `operator` | CombinedFieldsOperator | `And`/`Or` |
| `minimumShouldMatch` | String | 예: `2`, `75%` |
| `autoGenerateSynonymsPhraseQuery` | Boolean | 구문 동의어 생성 |
| `boost` | Float | 가중치 |

Multi‑match (일반형)

| 옵션 | 타입 | 비고 |
|---|---|---|
| `type` | TextQueryType | `best_fields`, `most_fields`, `cross_fields`, `phrase(_prefix)`, `bool_prefix` |
| `operator` | Operator | 토큰 결합 방식 |
| `minimumShouldMatch` | String | 예: `2`, `75%` |
| `analyzer` | String | 질의어 분석기 |
| `slop` | Int | 구문 허용 간격 |
| `tieBreaker` | Double | `best_fields` 블렌딩 |
| `fuzziness` | String | `AUTO`, `1`, `2` |
| `prefixLength`/`maxExpansions` | Int | 퍼지/프리픽스 제어 |
| `lenient` | Boolean | 포맷 오류 무시 |
| `zeroTermsQuery` | ZeroTermsQuery | `All` 또는 `None` |

Query string

| 옵션 | 타입 | 비고 |
|---|---|---|
| `fields`/`defaultField` | List<String>/String | 대상/기본 필드 |
| `analyzer`/`quoteAnalyzer` | String | 분석기 지정 |
| `quoteFieldSuffix` | String | 따옴표 용어 접미사 |
| `defaultOperator` | Operator | `And`/`Or` |
| `allowLeadingWildcard` | Boolean | `*term` 허용(비용 큼) |
| `analyzeWildcard` | Boolean | 와일드카드 분석 |
| `fuzziness` | String | 퍼지 레벨 |
| `fuzzyMaxExpansions`/`fuzzyPrefixLength` | Int | 퍼지 제어 |
| `fuzzyTranspositions` | Boolean | 전치 허용 |
| `minimumShouldMatch` | String | 예: `2`, `75%` |
| `phraseSlop` | Double | 구문 간격 |
| `lenient` | Boolean | 포맷 오류 무시 |

Simple query string

| 옵션 | 타입 | 비고 |
|---|---|---|
| `fields` | List<String> | 대상 필드 |
| `defaultOperator` | Operator | `And`/`Or` |
| `analyzer` | String | 분석기 지정 |
| `quoteFieldSuffix` | String | 따옴표 용어 접미사 |
| `analyzeWildcard` | Boolean | 와일드카드 분석 |
| `flags` | List<SimpleQueryStringFlag> | 예: `Prefix`, `Phrase`, `And`, `Or`, `All` |
| `fuzzyMaxExpansions`/`fuzzyPrefixLength` | Int | 퍼지 제어 |
| `fuzzyTranspositions` | Boolean | 전치 허용 |
| `minimumShouldMatch` | String | 예: `2`, `75%` |
| `lenient` | Boolean | 포맷 오류 무시 |
