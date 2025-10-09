# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Elasticsearch Dynamic Query DSL is a type-safe Kotlin library that provides a fluent DSL for building Elasticsearch queries and aggregations. The library wraps the official `elasticsearch-java` client and automatically omits null/empty values to produce minimal, valid JSON.

## Commands

### Build & Test
```bash
# Full build with tests
./gradlew clean build

# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests MatchQueryTest

# Run tests for specific package
./gradlew test --tests "com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.*"

# Check (includes compilation, tests, and verification)
./gradlew check

# Local Maven installation for testing
./gradlew publishToMavenLocal
```

### Publishing
```bash
# Publish to local Maven repository for testing
./gradlew publishToMavenLocal

# Verify published POM file
cat ~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom

# Publish to GitHub Packages
./gradlew publishToGitHub

# Publish to Maven Central (release versions only, not SNAPSHOT)
./gradlew publishToCentral
```

### Publishing Configuration

The project uses `maven-publish` plugin with the `java-library` plugin for proper transitive dependency management:

```kotlin
plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`  // Key plugin for transitive dependencies
    `maven-publish`
}

dependencies {
    // API dependencies (exposed to consumers, compile scope)
    api("co.elastic.clients:elasticsearch-java:$elasticsearchJavaVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Implementation dependencies (internal only, runtime scope)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            // java-library plugin automatically handles api→compile, implementation→runtime
        }
    }
}
```

**No explicit `pom.withXml` configuration is needed** - the `java-library` plugin automatically:
- Maps `api` dependencies to compile scope (transitive)
- Maps `implementation` dependencies to runtime scope

This ensures that consumers only need to add one dependency and automatically get:
- `co.elastic.clients:elasticsearch-java` (compile scope, transitive)
- `org.jetbrains.kotlinx:kotlinx-coroutines-core` (compile scope, transitive)

**Verification**: Use `./verify-pom.sh` or check the POM file directly:
```bash
cat ~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom
```

## Architecture

### Core DSL Pattern

The library follows a consistent DSL builder pattern across all query types:

1. **Entry Points** (`core/QueryDsl.kt`):
   - `query { }` - Creates a Query, throws if invalid
   - `queryOrNull { }` - Returns null for invalid queries (e.g., missing required fields)

2. **SubQueryBuilders** (`core/SubQueryBuilders.kt`):
   - Accumulates multiple sub-queries within bool clauses
   - Supports three addition patterns:
     * Sequential: `termQuery { }; rangeQuery { }`
     * Bracket batch: `queries[{ termQuery {} }, { rangeQuery {} }]`
     * Unary plus: `+queryOrNull { termQuery {} }`

3. **Clause Helpers** (`clauses/`):
   - `mustQuery { }`, `filterQuery { }`, `shouldQuery { }`, `mustNotQuery { }`
   - Wrap SubQueryBuilders to collect queries for bool clauses
   - Automatically filter out null queries

### Query Organization

Queries are organized by Elasticsearch type:

- **`queries/termlevel/`**: term, terms, range, exists, prefix, wildcard, regexp, fuzzy, ids
- **`queries/fulltext/`**: match (13 parameters), matchPhrase, multiMatch, queryString, etc.
- **`queries/compound/`**: bool, boosting, constantScore, disMax, functionScore
- **`queries/span/`**: spanTerm, spanNear, spanOr, spanFirst, spanContaining, etc.
- **`queries/specialized/`**: script, scriptScore, wrapper, pinned, percolate, knn, rule, weightedTokens
- **`queries/geo/`**: geoBoundingBox, geoDistance, geoPolygon, geoShape
- **`queries/joining/`**: nested, hasChild, hasParent

### Aggregations

- **`aggregations/AggregationsDsl.kt`**: Entry point `aggregations { }`
- **`aggregations/BucketAggregations.kt`**: terms, dateHistogram, composite, sampler, etc.
- **`aggregations/MetricsAggregations.kt`**: avg, sum, cardinality, percentiles, stats, etc.
- **`aggregations/PipelineAggregations.kt`**: Pipeline aggregations like cumulative sum

### Null/Empty Handling

All query builders use `.takeIf { it.isNotBlank() }` pattern to automatically omit:
- Null values
- Empty strings
- Empty collections

Example from `MatchQueryDsl.kt`:
```kotlin
val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
val q = dsl.query?.takeIf { it.isNotBlank() } ?: return this
```

## DSL Class Pattern

When adding a new query type, follow this pattern:

1. **Create DSL class** with nullable properties:
```kotlin
class MatchQueryDsl {
    // Required parameters
    var field: String? = null
    var query: String? = null

    // Optional parameters grouped by category
    var operator: Operator? = null
    var boost: Float? = null
}
```

2. **Create extension function** on `Query.Builder`:
```kotlin
fun Query.Builder.matchQuery(fn: MatchQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = MatchQueryDsl().apply(fn)

    // Validate required fields, return early if missing
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val q = dsl.query?.takeIf { it.isNotBlank() } ?: return this

    // Build Elasticsearch query
    return this.match { m ->
        m.field(f).query(q)
        dsl.operator?.let { m.operator(it) }
        dsl.boost?.let { m.boost(it) }
        m
    }
}
```

3. **Add to SubQueryBuilders** for clause compatibility

4. **Write tests first** (TDD):
   - Test all bool contexts (must, filter, mustNot, should)
   - Test null/empty value handling
   - Test all parameters
   - Test parameter combinations
   - Minimum 15-20 test cases for comprehensive coverage

## Testing Guidelines

### Test Structure (Kotest FunSpec)
```kotlin
class MatchQueryTest : FunSpec({
    test("한글 테스트 이름으로 명확한 의도 표현") {
        // Given
        val query = query {
            boolQuery {
                mustQuery { matchQuery { field = "a"; query = "1111" } }
            }
        }

        // Then
        query.isBool shouldBe true
        val mustQuery = query.bool().must()
        mustQuery.size shouldBe 1
    }
})
```

### Test Categories
- **Bool context tests**: Verify query works in must/filter/mustNot/should
- **Null handling tests**: Verify null/empty values are automatically filtered
- **Parameter tests**: Test each parameter individually
- **Combination tests**: Test multiple parameters together
- **Edge case tests**: Independent usage, empty collections, etc.

## Constitution Principles

This project follows strict constitutional principles (`.specify/memory/constitution.md`):

1. **타입 안정성 우선**: Use Kotlin type system, avoid JSON strings
2. **자동 정제**: Automatically filter null/empty values
3. **테스트 주도 개발 (필수)**: Red-Green-Refactor cycle strictly enforced
4. **완전한 Elasticsearch 커버리지**: Support all Elasticsearch features
5. **라이브러리 우선**: Independent, reusable library design

### Transitive Dependencies

The library uses `java-library` plugin with proper dependency scoping:
- **`api`**: elasticsearch-java, kotlinx-coroutines-core (exposed in public API)
- **`implementation`**: jackson-databind (internal only)

Users only need to add this library; elasticsearch-java is automatically included.

## Match Query as Reference

MatchQueryDsl serves as a reference implementation with:
- 13 parameters (all Elasticsearch Match query parameters)
- Grouped by category (required, basic, fuzzy, advanced, common)
- Comprehensive KDoc documentation
- 21 test cases covering all scenarios
- 100% test coverage

Location: `src/main/kotlin/.../queries/fulltext/MatchQueryDsl.kt`

## Client Integration

The library provides a client wrapper (`client/ElasticsearchClient.kt`) with:
- Synchronous search operations
- Async search with coroutines
- Index management
- Document indexing/updating/deletion

## File Naming Conventions

- Query DSL classes: `*QueryDsl.kt` or `*Query.kt`
- Test files: `*Test.kt` (mirrors production structure)
- Aggregation files: `*Aggregations.kt` or `*AggregationDsl.kt`
