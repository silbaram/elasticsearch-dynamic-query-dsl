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
simpleQueryStringQuery("kotlin +coroutine | \"structured query\"", listOf("title","body"))
```

Small JSON
```json
{ "query": { "bool": { "must": [{ "term": { "user.id": "silbaram" }}] } } }
{ "query": { "match_phrase": { "title": { "query": "exact order", "slop": 1 } } } }
{ "query": { "combined_fields": { "query": "john smith", "fields": ["first_name","last_name"], "operator": "and", "minimum_should_match": "2" } } }
```

### Multi‑match (general)
Apply a single query string across multiple fields; use `multiMatchQuery` or `Query.Builder.multiMatch`. Supports types: `best_fields`, `most_fields`, `cross_fields`, `phrase(_prefix)`, `bool_prefix`.

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
Lucene query syntax across one or multiple fields. Supports quoted-field analyzer/suffix, wildcards (with `analyzeWildcard`/`allowLeadingWildcard`), fuzziness and phrase slop.

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

### Simple query string
Forgiving Lucene-like syntax that never throws on parse; unsupported/invalid parts are skipped. Supports flags (e.g., `Prefix`, `Phrase`, `And`, `Or`, `All`).

```kotlin
simpleQueryStringQuery(
  query = "kotlin +coroutine | \"structured query\"",
  fields = listOf("title^2","body"),
  // common options
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

See tests: [SimpleQueryStringQueryTest.kt](src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/fulltext/SimpleQueryStringQueryTest.kt)

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

## Advanced Options Summary

Combined fields (combined_fields)

| Option | Type | Notes |
|---|---|---|
| `fields` | List<String> | Target fields (weights via `^`) |
| `operator` | CombinedFieldsOperator | `And` or `Or` |
| `minimumShouldMatch` | String | e.g., `2`, `75%` |
| `autoGenerateSynonymsPhraseQuery` | Boolean | Phrase synonyms |
| `boost` | Float | Weight |

Multi‑match (general)

| Option | Type | Notes |
|---|---|---|
| `type` | TextQueryType | `best_fields`, `most_fields`, `cross_fields`, `phrase(_prefix)`, `bool_prefix` |
| `operator` | Operator | Token combine mode |
| `minimumShouldMatch` | String | e.g., `2`, `75%` |
| `analyzer` | String | Query analyzer |
| `slop` | Int | Phrase slack |
| `tieBreaker` | Double | `best_fields` blending |
| `fuzziness` | String | `AUTO`, `1`, `2` |
| `prefixLength`/`maxExpansions` | Int | Fuzzy/prefix controls |
| `lenient` | Boolean | Ignore format errors |
| `zeroTermsQuery` | ZeroTermsQuery | `All` or `None` |

Query string

| Option | Type | Notes |
|---|---|---|
| `fields`/`defaultField` | List<String>/String | Target fields / default |
| `analyzer`/`quoteAnalyzer` | String | Analyzer overrides |
| `quoteFieldSuffix` | String | Suffix for quoted terms |
| `defaultOperator` | Operator | `And`/`Or` |
| `allowLeadingWildcard` | Boolean | Enable `*term` (expensive) |
| `analyzeWildcard` | Boolean | Analyze wildcards |
| `fuzziness` | String | Fuzzy level |
| `fuzzyMaxExpansions`/`fuzzyPrefixLength` | Int | Fuzzy controls |
| `fuzzyTranspositions` | Boolean | Fuzzy transpositions |
| `minimumShouldMatch` | String | e.g., `2`, `75%` |
| `phraseSlop` | Double | Phrase slack |
| `lenient` | Boolean | Ignore format errors |

Simple query string

| Option | Type | Notes |
|---|---|---|
| `fields` | List<String> | Target fields |
| `defaultOperator` | Operator | `And`/`Or` |
| `analyzer` | String | Analyzer override |
| `quoteFieldSuffix` | String | Suffix for quoted terms |
| `analyzeWildcard` | Boolean | Analyze wildcards |
| `flags` | List<SimpleQueryStringFlag> | e.g., `Prefix`, `Phrase`, `And`, `Or`, `All` |
| `fuzzyMaxExpansions`/`fuzzyPrefixLength` | Int | Fuzzy controls |
| `fuzzyTranspositions` | Boolean | Fuzzy transpositions |
| `minimumShouldMatch` | String | e.g., `2`, `75%` |
| `lenient` | Boolean | Ignore format errors |
