# Elasticsearch Dynamic Query DSL for Kotlin

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Type-safe Kotlin DSL for building Elasticsearch queries dynamically. Compose queries with concise builders that omit null/blank inputs, keeping the emitted JSON minimal and valid.

Read this in Korean: README.ko.md

## Features
- Intuitive DSL: Kotlin builders instead of raw JSON.
- Dynamic omission: Skip invalid inputs automatically.
- Full‑text helpers: match, match_phrase, match_bool_prefix, multi_match(type=phrase), combined_fields.
- Function score: field value factor, script score, weight, random score, decay.
- Kotlin/JDK 17; Kotest + JUnit 5.

## Quick Start
- Build/test: `./gradlew clean build`
- Publish locally: `./gradlew publishToMavenLocal`

Minimal example
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

## Usage (brief)
- Bool + clauses
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

- Full‑text (one‑liners)
```kotlin
matchPhraseQuery("title", "exact order", slop = 1)
matchBoolPrefixQuery(field = "title", query = "quick bro")
multiMatchPhraseQuery("kotlin coroutine", listOf("title^2", "description"))
queryStringQuery("kotlin* AND \"structured query\"", listOf("title","body"))
```

Small JSON
```json
{ "query": { "bool": { "must": [{ "term": { "user.id": "silbaram" }}] } } }
{ "query": { "match_phrase": { "title": { "query": "exact order", "slop": 1 } } } }
{ "query": { "combined_fields": { "query": "john smith", "fields": ["first_name","last_name"], "operator": "and", "minimum_should_match": "2" } } }
```

### Multi‑match (general)
Use `multiMatchQuery` or `Query.Builder.multiMatch`.

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

See tests: [MultiMatchQueryTest.kt](src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/fulltext/MultiMatchQueryTest.kt)

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

Notes: Use text fields; null/blank inputs are omitted.

### Query string
Lucene query syntax across one or multiple fields.

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

See tests: [QueryStringQueryTest.kt](src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/fulltext/QueryStringQueryTest.kt)

## Function Score
Compose per‑function filters, field value factor, weight, random, and decay.

See tests:
- Core: [FunctionScoreTest.kt](src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/compound/FunctionScoreTest.kt)
- Kibana‑like: [FunctionScoreKibanaParityTest.kt](src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/compound/FunctionScoreKibanaParityTest.kt)
- Decay: [DecayFunctionTest.kt](src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/compound/DecayFunctionTest.kt)

## Project Structure
- `src/main/kotlin`: Core DSL and query builders.
- `src/test/kotlin`: Kotest specs on JUnit 5.
- Gradle Kotlin DSL; JDK 17 toolchain.

## License
Apache License 2.0 — see LICENSE.

## Badges & Release
- Build/test: `./gradlew clean build`
- Publish to Maven Local: `./gradlew publishToMavenLocal`
- Dependency (example)
```kotlin
repositories { mavenLocal(); mavenCentral() }
dependencies { implementation("com.github.silbaram:elasticsearch-dynamic-query-dsl:1.0-SNAPSHOT") }
```
