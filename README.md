# Elasticsearch Dynamic Query DSL for Kotlin

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Type-safe Kotlin DSL for composing Elasticsearch queries. Builders omit null or blank inputs so that generated JSON stays minimal and valid. 한글 문서는 [`README.ko.md`](README.ko.md)에서 확인할 수 있습니다.

## Highlights
- **Fluent Kotlin API** – Prefer Kotlin builders over brittle JSON strings.
- **Safe omission** – Invalid or empty values get dropped automatically.
- **Rich coverage** – Full-text, term-level, span, compound, and specialized queries (percolate, KNN, script, script_score, wrapper, pinned, rule, weighted_tokens, rank_feature, distance_feature).
- **Aggregation DSL** – Compose bucket and metric aggregations (terms/date histogram/composite plus boxplot, cardinality, extended stats, geo bounds/centroid/line, matrix stats, MAD, percentiles, percentile ranks, rate, scripted metric, stats, string stats, t-test, top hits/metrics, weighted avg, etc.) with the same omission safeguards.
- **Composable helpers** – `SubQueryBuilders` utilities let you stack clauses without repetitive `query { ... }` blocks.
- **Battle-tested** – Kotest + JUnit 5 specs mirror the production package layout.

## Requirements
- JDK 17
- Gradle Wrapper (provided)

## Getting Started
```bash
./gradlew clean build        # compile + full test suite
./gradlew test               # iterative feedback with Kotest/JUnit
./gradlew publishToMavenLocal # install DSL into ~/.m2 for local experiments
```

### Minimal Example
```kotlin
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.*

val q: Query = query {
    boolQuery {
        mustQuery {
            termQuery { field = "user.id"; value = "silbaram" }
        }
        filterQuery {
            rangeQuery { field = "age"; gte = 20; lt = 35 }
        }
        shouldQuery {
            queries[
                { termQuery { field = "tags"; value = "kotlin" } },
                { termQuery { field = "tags"; value = "search" } }
            ]
        }
    }
}
```

## DSL Overview
### Core Patterns
- Build top-level queries with `query { ... }` or `queryOrNull { ... }` (omits invalid content).
- Use clause helpers (`mustQuery`, `filterQuery`, `shouldQuery`, `mustNotQuery`) to aggregate sub-queries.
- `SubQueryBuilders` exposes inline helpers such as `termQuery`, `rangeQuery`, `matchQuery`, `scriptQuery`, `scriptScoreQuery`, `wrapperQuery`, `pinnedQuery`, etc.

### Term & Full-text Helpers
- `termQuery`, `termsQuery`, `rangeQuery`, `existsQuery`, `matchAllDsl`
- `matchQuery`, `matchPhrase`, `matchPhrasePrefix`, `matchBoolPrefix`
- `multiMatchQuery`, `combinedFields`, `queryString`, `simpleQueryString`
- `moreLikeThis`, `intervals`, and span DSL (`spanTermQuery`, `spanNearQuery`, ...)

See the test suite under `src/test/kotlin/.../queries/{termlevel,fulltext,span}` for end-to-end samples.

### Compound Builders
- `boolQuery` with clause helpers
- `functionScore` (field value factor, script score, random score, weight)
- `constantScore`, `boostingQuery`

## Specialized Queries
Recent additions extend the DSL beyond core helpers:

```kotlin
// Script query (inline or stored)
query {
    scriptQuery {
        inline(
            source = "doc['votes'].value > params.threshold",
            lang = "painless",
            params = mapOf("threshold" to 10)
        )
        boost = 1.2f
        _name = "votes-script"
    }
}

// Script score query with default organic match_all
query {
    scriptScoreQuery {
        inline(source = "params.factor", params = mapOf("factor" to 2))
        minScore = 0.5f
    }
}

// Wrapper query: supply raw JSON or pre-encoded payload
query {
    wrapperQuery {
        rawJson("""{"match":{"status":"active"}}""")
    }
}

// Pinned query combining curated ids with an organic fallback
query {
    pinnedQuery {
        ids("1", "2", "3")
        organic {
            matchQuery {
                field = "title"
                query = "elasticsearch"
            }
        }
    }
}

// Rule query ties rulesets to an organic fallback with match criteria
query {
    ruleQueryDsl {
        rulesetIds("featured")
        organic {
            matchQuery {
                field = "status"
                query = "active"
            }
        }
        matchCriteria(mapOf("channel" to "web"))
    }
}

// Weighted tokens query describing weighted semantic tokens for a field
query {
    weightedTokensQuery {
        field = "title.embedding"
        tokens(
            "kotlin" to 1.0,
            "dsl" to 0.7
        )
        pruningConfig {
            tokensFreqRatioThreshold = 3
            tokensWeightThreshold = 0.2f
        }
    }
}
```

Other specialized builders include `knnQuery`, `percolateQuery`, `rankFeatureQuery`, and `distanceFeatureQuery`, plus span integrations. Each has focused specs in `src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/queries/specialized`.

## Aggregations DSL

Use `aggregations { ... }` to build the aggregation map expected by Elasticsearch search requests. Builders mirror server-side options, skip blank values, and let you attach metadata or nested aggregations.

```kotlin
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval
import com.github.silbaram.elasticsearch.dynamic_query_dsl.aggregations.*

val aggs = aggregations {
    terms("top_tags") {
        field = "tags.keyword"
        size = 5
        orderByCount()
    }
    dateHistogram("posts_per_day") {
        field("published_at")
        calendarInterval(CalendarInterval.Day)
        aggregations {
            avg("avg_score") { field = "score" }
        }
    }
    randomSampler("sample_bucket", probability = 0.1, seed = 42L)
    timeSeries("daily_series", size = 30, keyed = true)
    percentiles("score_percentiles") {
        field = "score"
        percents(25.0, 50.0, 90.0)
    }
    matrixStats("price_quantity") {
        field("price")
        field("quantity")
    }
    tTest("variant_test") {
        populationA { field = "metric_a" }
        populationB { field = "metric_b" }
    }
}
```

Bucket builders cover adjacency matrix, composite, geo grids, range variants, samplers, and more. Metric helpers span the full Elasticsearch surface (`avg`, `sum`, `min`, `max`, `valueCount`, `boxplot`, `cardinality`, `extendedStats`, `geoBounds`, `geoCentroid`, `geoLine`, `matrixStats`, `medianAbsoluteDeviation`, `percentiles`, `percentileRanks`, `rate`, `scriptedMetric`, `stats`, `stringStats`, `tTest`, `topHits`, `topMetrics`, `weightedAvg`, etc.) while keeping omission safeguards. See `src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/aggregations/BucketAggregationsTest.kt` and `src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/aggregations/MetricsAggregationsTest.kt` for end-to-end samples.

## Testing & Quality
- Run targeted suites via Gradle’s `--tests` flag when iterating.
- `./gradlew check` executes compilation, tests, and additional verification tasks.
- Kotest specs use descriptive `context/should` blocks and mirror production packages for easy discovery.

## Contributing
1. Create a feature branch following Conventional Commit scopes (e.g., `feat/pinned-query`).
2. Add or update tests for any behavioural change.
3. Ensure `./gradlew check` passes before opening a PR.
4. Summarize motivation, DSL snippets, and expected JSON in the PR body. Link issues with `Fixes #123` when applicable.

## License
Apache License 2.0. See [LICENSE](LICENSE) for details.
