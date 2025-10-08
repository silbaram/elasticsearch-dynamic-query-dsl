# Kotlin용 Elasticsearch 동적 쿼리 DSL

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

타입 세이프 코틀린 DSL로 Elasticsearch 쿼리를 조합할 수 있는 라이브러리입니다. null/빈 값은 자동으로 제외되어 결과 JSON이 간결하고 유효하게 유지됩니다. English version is available in [`README.md`](README.md).

## 핵심 특징
- **코틀린 친화적 API**: JSON 문자열 대신 빌더 패턴으로 쿼리를 작성합니다.
- **안전한 생략 처리**: 불필요하거나 잘못된 입력을 자동으로 걸러냅니다.
- **폭넓은 쿼리 지원**: 전문 검색, term-level, span, compound, script, wrapper, pinned, rule, weighted_tokens 등 다양한 Elasticsearch DSL을 커버합니다.
- **완전한 Match 쿼리 지원**: 13개 파라미터(query, operator, fuzziness, analyzer, minimumShouldMatch 등) 전체를 지원하며 21개 이상의 테스트 케이스로 모든 Elasticsearch Match 쿼리 기능을 검증합니다.
- **Aggregation DSL**: terms/date histogram/composite/random sampler/time series 등 다양한 버킷 집계와 boxplot, cardinality, extended stats, geo bounds/centroid/line, matrix stats, MAD, percentiles, percentile ranks, rate, scripted metric, stats, string stats, t-test, top hits/metrics, weighted avg 등 메트릭 집계를 동일한 생략 규칙으로 구성합니다.
- **재사용 가능한 헬퍼**: `SubQueryBuilders`로 bool 절 내부에서도 간단히 하위 쿼리를 누적할 수 있습니다. 서브 쿼리는 순차 호출, `queries[...]` 대괄호, `+query` 중 원하는 방식으로 추가할 수 있습니다.
- **테스트 검증**: Kotest + JUnit 5 스펙이 패키지 구조와 동일하게 구성되어 있으며, 핵심 쿼리 빌더는 100% 테스트 커버리지를 달성했습니다.

## 요구 사항
- JDK 21 (toolchain 설정됨)
- Gradle Wrapper (저장소에 포함)
- Kotlin 2.0.20

## 시작하기

### 설치 방법
Gradle 프로젝트에 라이브러리를 추가하세요:

```kotlin
dependencies {
    implementation("io.github.silbaram:elasticsearch-dynamic-query-dsl:1.0.0")
    // elasticsearch-java는 전이 의존성으로 자동 포함됩니다
    // 별도로 추가할 필요가 없습니다!
}
```

### 소스에서 빌드
```bash
./gradlew clean build        # 전체 빌드 및 테스트
./gradlew test               # 반복 개발 시 빠른 테스트
./gradlew publishToMavenLocal # ~/.m2 로컬 배포
```

### 최소 예제
```kotlin
val q: Query = query {
    boolQuery {
        mustQuery {
            termQuery { field = "user.id"; value = "silbaram" }
            boolQuery {
                shouldQuery {
                    termQuery { field = "tags"; value = "kotlin" }
                    termQuery { field = "tags"; value = "dsl" }
                }
            }
        }
        filterQuery { rangeQuery { field = "age"; gte = 20; lt = 35 } }
        shouldQuery {
            termQuery { field = "tags"; value = "search" }
            boolQuery {
                shouldQuery {
                    termQuery { field = "interests"; value = "es" }
                    termQuery { field = "interests"; value = "dsl" }
                }
            }
        }
        mustNotQuery { existsQuery { field = "deleted_at" } }
    }
}

// 위 예시는 절 내부에서 순차적으로 헬퍼를 호출해 중첩 bool 쿼리를 구성하는 기본 패턴을 보여줍니다.
```

## DSL 개요
### 기본 패턴
- 최상위는 `query { ... }` 또는 `queryOrNull { ... }` 를 사용합니다.
- `mustQuery`, `filterQuery`, `shouldQuery`, `mustNotQuery` 등 절 전용 헬퍼로 서브 쿼리를 누적합니다.
- `SubQueryBuilders`는 `termQuery`, `rangeQuery`, `matchQuery`, `scriptQuery`, `scriptScoreQuery`, `wrapperQuery`, `pinnedQuery` 등 자주 쓰는 빌더를 바로 노출합니다.

#### 여러 하위 쿼리 누적하기
- **순차 호출**: 절 블록 안에서 헬퍼를 연속 호출하면 유효한 쿼리가 자동으로 수집됩니다.
- **대괄호 배치**: `queries[...]`를 사용해 여러 빌더 또는 미리 만들어둔 `Query?` 인스턴스를 한 번에 추가합니다.
- **단항 플러스**: `+queryOrNull { ... }` 혹은 `+사전_생성_쿼리` 형태로 표현식 기반 누적도 가능합니다.

```kotlin
mustQuery {
    termQuery { field = "status"; value = "active" }

    queries[
        {
            termQuery { field = "tier"; value = "gold" }
        },
        queryOrNull { termQuery { field = "region"; value = regionIfAny } }
    ]

    +queryOrNull { matchQuery { field = "description"; query = keyword } }
}
```

### 자주 쓰는 쿼리 빌더
- **Term/Range**: `termQuery`, `termsQuery`, `rangeQuery`, `existsQuery`, `matchAllDsl`
- **`matchQuery` – 13개 파라미터 완전 지원**:
  - 기본: `field`, `query`, `analyzer`, `operator`, `minimumShouldMatch`
  - 퍼지 매칭: `fuzziness`, `prefixLength`, `maxExpansions`, `fuzzyTranspositions`, `fuzzyRewrite`
  - 고급: `autoGenerateSynonymsPhraseQuery`, `lenient`, `zeroTermsQuery`
  - 공통: `boost`, `_name`
- **전문 검색**: `matchPhrase`, `matchPhrasePrefix`, `matchBoolPrefix`, `multiMatchQuery`, `combinedFields`, `queryString`, `simpleQueryString`
- **Span/Interval**: `spanTermQuery`, `spanNearQuery`, `spanContainingQuery`, `intervals`

```kotlin
// 여러 파라미터를 조합한 Match 쿼리
query {
    matchQuery {
        field = "content"
        query = "엘라스틱서치 검색"
        operator = Operator.And
        fuzziness = "AUTO"
        analyzer = "standard"
        boost = 1.5F
        _name = "메인_검색"
    }
}

// Match 쿼리는 Bool 쿼리 없이도 독립적으로 사용 가능
query {
    matchQuery {
        field = "title"
        query = "코틀린 DSL"
    }
}
```

세부 예제는 `src/test/kotlin/.../queries/{termlevel,fulltext,span}` 경로의 Kotest 스펙을 참고하세요. `MatchQueryTest`에는 모든 파라미터와 엣지 케이스를 다루는 21개의 포괄적인 테스트가 포함되어 있습니다.

### 컴파운드/스코어링 빌더
- `boolQuery` + 절 헬퍼
- `functionScore` (field value factor, script score, random score, weight 등)
- `constantScore`, `boostingQuery`

## 특수 쿼리 모음
최근 추가된 스페셜 DSL은 다음과 같습니다.

```kotlin
// Script 쿼리 (inline/stored 모두 지원)
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

// Script score 쿼리 (organic 기본값은 match_all)
query {
    scriptScoreQuery {
        inline(source = "params.factor", params = mapOf("factor" to 2))
        minScore = 0.5f
    }
}

// Wrapper 쿼리: 원본 JSON 또는 base64 문자열 제공
query {
    wrapperQuery {
        rawJson("""{"match":{"status":"active"}}""")
    }
}

// Pinned 쿼리: 고정 문서와 일반 organic 쿼리 결합
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

// Rule 쿼리: 룰셋과 organic 쿼리를 매칭 기준과 함께 연결
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

// Weighted tokens 쿼리: 필드에 대해 가중 토큰을 제공
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

그 외 `knnQuery`, `percolateQuery`, `rankFeatureQuery`, `distanceFeatureQuery`, `ruleQuery` 등도 지원하며, 관련 예제는 `src/test/kotlin/.../queries/specialized`에서 확인할 수 있습니다.

## Aggregations DSL

`aggregations { ... }` 블록을 사용하면 검색 요청에 전달할 집계 맵을 Kotlin DSL로 작성할 수 있습니다. 서버 옵션을 그대로 노출하면서도 빈 값은 자동으로 생략되고, 메타데이터와 서브 집계도 간단히 추가할 수 있습니다.

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

버킷 빌더는 adjacency matrix, composite, geo grid, range, sampler 등을 폭넓게 지원합니다. 메트릭 헬퍼 역시 `avg`, `sum`, `min`, `max`, `valueCount`, `boxplot`, `cardinality`, `extendedStats`, `geoBounds`, `geoCentroid`, `geoLine`, `matrixStats`, `medianAbsoluteDeviation`, `percentiles`, `percentileRanks`, `rate`, `scriptedMetric`, `stats`, `stringStats`, `tTest`, `topHits`, `topMetrics`, `weightedAvg` 등 전체 Elasticsearch 집계를 동일한 생략 규칙으로 제공합니다. 전체 예제는 `src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/aggregations/BucketAggregationsTest.kt`와 `src/test/kotlin/com/github/silbaram/elasticsearch/dynamic_query_dsl/aggregations/MetricsAggregationsTest.kt`를 참고하세요.

## 테스트 & 품질 관리
- 필요 시 `./gradlew test --tests "패키지.클래스"`로 특정 스펙만 실행하세요.
- `./gradlew check`는 컴파일, 테스트, 추가 검증 작업을 한번에 수행합니다.
- 모든 스펙은 `context`/`should` 형태로 작성되어 읽기 쉽고, 프로덕션 패키지 구조와 동일하게 배치되어 있습니다.

## 기여 가이드
1. Conventional Commit 규칙에 맞춘 브랜치를 생성하세요 (예: `feat/pinned-query`).
2. 동작 변경 시 반드시 테스트를 추가하거나 수정합니다.
3. PR 전 `./gradlew check`가 통과하는지 확인합니다.
4. PR 본문에는 동기, DSL 사용 예시, 기대 JSON, 관련 이슈(`Fixes #123`)를 정리해 주세요.

## 라이선스
Apache License 2.0. 자세한 내용은 [LICENSE](LICENSE)를 참고하세요.
