# Kotlin용 Elasticsearch 동적 쿼리 DSL

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

타입 세이프 코틀린 DSL로 Elasticsearch 쿼리를 조합할 수 있는 라이브러리입니다. null/빈 값은 자동으로 제외되어 결과 JSON이 간결하고 유효하게 유지됩니다. English version is available in [`README.md`](README.md).

## 핵심 특징
- **코틀린 친화적 API**: JSON 문자열 대신 빌더 패턴으로 쿼리를 작성합니다.
- **안전한 생략 처리**: 불필요하거나 잘못된 입력을 자동으로 걸러냅니다.
- **폭넓은 쿼리 지원**: 전문 검색, term-level, span, compound, script, wrapper, pinned, rule, weighted_tokens 등 다양한 Elasticsearch DSL을 커버합니다.
- **Aggregation DSL**: terms/date histogram/composite/random sampler/time series 등 다양한 버킷 집계와 boxplot, cardinality, extended stats, geo bounds/centroid/line, matrix stats, MAD, percentiles, percentile ranks, rate, scripted metric, stats, string stats, t-test, top hits/metrics, weighted avg 등 메트릭 집계를 동일한 생략 규칙으로 구성합니다.
- **재사용 가능한 헬퍼**: `SubQueryBuilders`로 bool 절 내부에서도 간단히 하위 쿼리를 누적할 수 있습니다.
- **Elasticsearch 클라이언트 통합**: 비동기 지원, 검색, 인덱싱, 인덱스 관리 기능을 포함한 내장 클라이언트 래퍼를 제공합니다.
- **테스트 검증**: Kotest + JUnit 5 스펙이 패키지 구조와 동일하게 구성되어 있어 예제와 검증을 동시에 제공합니다.

## 요구 사항
- JDK 17
- Gradle Wrapper (저장소에 포함)

## 시작하기

### 의존성 추가

```kotlin
dependencies {
    implementation("io.github.silbaram:elasticsearch-dynamic-query-dsl:1.0.0-SNAPSHOT")
    implementation("co.elastic.clients:elasticsearch-java:8.14.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // 비동기 지원시
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2") // JSON 처리
}
```

### 빌드 & 테스트

```bash
./gradlew clean build        # 전체 빌드 및 테스트
./gradlew test               # 반복 개발 시 빠른 테스트
./gradlew publishToMavenLocal # ~/.m2 로컬 배포
```

### 최소 예제
```kotlin
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.*

val q: Query = query {
    boolQuery {
        mustQuery { termQuery { field = "user.id"; value = "silbaram" } }
        filterQuery { rangeQuery { field = "age"; gte = 20; lt = 35 } }
        shouldQuery {
            queries[
                { termQuery { field = "tags"; value = "kotlin" } },
                { termQuery { field = "tags"; value = "search" } }
            ]
        }
        mustNotQuery { existsQuery { field = "deleted_at" } }
    }
}
```

## DSL 개요
### 기본 패턴
- 최상위는 `query { ... }` 또는 `queryOrNull { ... }` 를 사용합니다.
- `mustQuery`, `filterQuery`, `shouldQuery`, `mustNotQuery` 등 절 전용 헬퍼로 서브 쿼리를 누적합니다.
- `SubQueryBuilders`는 `termQuery`, `rangeQuery`, `matchQuery`, `scriptQuery`, `scriptScoreQuery`, `wrapperQuery`, `pinnedQuery` 등 자주 쓰는 빌더를 바로 노출합니다.

### 자주 쓰는 쿼리 빌더
- **Term/Range**: `termQuery`, `termsQuery`, `rangeQuery`, `existsQuery`, `matchAllDsl`
- **전문 검색**: `matchQuery`, `matchPhrase`, `matchPhrasePrefix`, `matchBoolPrefix`, `multiMatchQuery`, `combinedFields`, `queryString`, `simpleQueryString`
- **Span/Interval**: `spanTermQuery`, `spanNearQuery`, `spanContainingQuery`, `intervals`

세부 예제는 `src/test/kotlin/.../queries/{termlevel,fulltext,span}` 경로의 Kotest 스펙을 참고하세요.

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

버킷 빌더는 adjacency matrix, composite, geo grid, range, sampler 등을 폭넓게 지원합니다. 메트릭 헬퍼 역시 `avg`, `sum`, `min`, `max`, `valueCount`, `boxplot`, `cardinality`, `extendedStats`, `geoBounds`, `geoCentroid`, `geoLine`, `matrixStats`, `medianAbsoluteDeviation`, `percentiles`, `percentileRanks`, `rate`, `scriptedMetric`, `stats`, `stringStats`, `tTest`, `topHits`, `topMetrics`, `weightedAvg` 등 전체 Elasticsearch 집계를 동일한 생략 규칙으로 제공합니다.

**예제 및 테스트**: 다음에서 포괄적인 예제를 확인하세요:
- `ElasticsearchClientExamplesTest.kt` - 클라이언트 사용법 예제 및 패턴
- `ElasticsearchClientTest.kt` - 실제 Elasticsearch와의 완전한 통합 테스트  
- `BucketAggregationsTest.kt` 및 `MetricsAggregationsTest.kt` - 집계 예제

## Elasticsearch 클라이언트 통합

라이브러리에는 Elasticsearch 클러스터에 대해 쿼리를 쉽게 실행할 수 있는 포괄적인 클라이언트 래퍼가 포함되어 있습니다. 이 클라이언트는 공식 `elasticsearch-java` 클라이언트를 Kotlin 친화적인 DSL로 래핑합니다.

### 클라이언트 빠른 시작

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.client.*

// 클라이언트 생성
val client = ElasticsearchClientWrapper.create() // 로컬 기본 설정
// 또는
val client = ElasticsearchClientWrapper.createFromEnvironment() // 환경변수에서

data class Product(val id: String, val name: String, val category: String, val price: Double)

// DSL을 사용한 검색
val response = client.search<Product> {
    indices("products")
    query {
        boolQuery {
            mustQuery {
                matchQuery {
                    field = "name"
                    query = "laptop"
                }
            }
            mustQuery {
                rangeQuery {
                    field = "price"
                    gte = 100.0
                    lte = 2000.0
                }
            }
        }
    }
    sortByField("price", co.elastic.clients.elasticsearch._types.SortOrder.Desc)
    size(20)
}

println("${response.totalHits}개 제품을 찾았습니다")
response.sources().forEach { product ->
    println("${product.name}: $${product.price}")
}

client.close()
```

### 주요 기능

- **타입 안전한 검색**: 자동 JSON 매핑을 지원하는 제네릭 검색 메소드
- **비동기 지원**: `suspend` 함수를 사용한 코루틴 기반 비동기 작업
- **인덱스 관리**: 인덱스 생성, 삭제, 존재 확인
- **대량 작업**: 에러 처리를 지원하는 효율적인 배치 인덱싱
- **응답 헬퍼**: 페이징 정보, 히트 추출, 소스 필터링
- **설정**: 환경변수 기반 설정, SSL/TLS, 기본 인증 지원
- **클러스터 헬스**: 클러스터 상태 및 샤드 정보 모니터링

### 클라이언트 사용 예제

#### 1. 클라이언트 생성

```kotlin
// 로컬 개발 환경 (localhost:9200)
val client = ElasticsearchClientWrapper.create()

// 환경변수에서 설정 로드
val client = ElasticsearchClientWrapper.createFromEnvironment()

// 커스텀 설정
val config = ElasticsearchClientConfig(
    hosts = listOf("es1.example.com:9200", "es2.example.com:9200"),
    protocol = "https",
    username = "elastic",
    password = "your_password",
    enableSsl = true
)
val client = ElasticsearchClientWrapper.create(config)
```

#### 2. 환경변수 설정

```bash
export ELASTICSEARCH_HOSTS=es1.example.com:9200,es2.example.com:9200
export ELASTICSEARCH_PROTOCOL=https
export ELASTICSEARCH_USERNAME=elastic
export ELASTICSEARCH_PASSWORD=your_password
export ELASTICSEARCH_SSL_ENABLED=true
export ELASTICSEARCH_CONNECT_TIMEOUT=5000
export ELASTICSEARCH_SOCKET_TIMEOUT=60000
```

#### 3. 문서 검색

```kotlin
// 간단한 검색
val response = client.search<Product> {
    indices("products")
    query {
        matchQuery {
            field = "name"
            query = "laptop"
        }
    }
    size(10)
}

// 결과 추출
val products = response.sources()
products.forEach { println(it.name) }

// 첫 번째 결과만
val firstProduct = response.firstSource()

// 페이징 정보
val pagingInfo = response.getPagingInfo(currentPage = 1, pageSize = 10)
println("${pagingInfo.currentPage} 페이지 / 전체 ${pagingInfo.totalPages} 페이지")
```

#### 4. 비동기 검색 (코루틴)

```kotlin
import kotlinx.coroutines.*

runBlocking {
    // 단일 비동기 검색
    val response = client.searchAsync<Product> {
        indices("products")
        query { matchAllQuery() }
    }

    // 여러 검색을 병렬로 실행
    val laptops = async {
        client.searchAsync<Product> {
            indices("products")
            query {
                termQuery {
                    field = "category"
                    value = "laptop"
                }
            }
        }
    }

    val phones = async {
        client.searchAsync<Product> {
            indices("products")
            query {
                termQuery {
                    field = "category"
                    value = "phone"
                }
            }
        }
    }

    println("노트북: ${laptops.await().totalHits}개")
    println("휴대폰: ${phones.await().totalHits}개")
}
```

#### 5. 문서 인덱싱

```kotlin
// 단건 인덱싱
val productId = client.index(
    index = "products",
    document = Product("1", "Laptop", "electronics", 1200.0),
    id = "1" // 옵션, null이면 자동 생성
)

// 비동기 인덱싱
val id = client.indexAsync(
    index = "products",
    document = product
)

// 대량 인덱싱
val products = listOf(
    Product("1", "Laptop", "electronics", 1200.0),
    Product("2", "Mouse", "accessories", 25.0),
    Product("3", "Keyboard", "accessories", 80.0)
)

val result = client.bulkIndex(
    index = "products",
    documents = products,
    idExtractor = { it.id }
)

println("${result.successCount}개 문서 인덱싱 완료")
if (result.hasErrors) {
    result.failedItems.forEach { item ->
        println("실패: ${item.id} - ${item.error}")
    }
}
```

#### 6. 인덱스 관리

```kotlin
// 인덱스 존재 확인
if (!client.indexExists("products")) {
    // 매핑과 설정으로 인덱스 생성
    client.createIndex(
        index = "products",
        mappings = mapOf(
            "name" to mapOf("type" to "text"),
            "price" to mapOf("type" to "double"),
            "category" to mapOf("type" to "keyword")
        ),
        settings = mapOf(
            "number_of_shards" to 2,
            "number_of_replicas" to 1
        )
    )
}

// 인덱스 삭제
client.deleteIndex("products")
```

#### 7. 클러스터 헬스 모니터링

```kotlin
val health = client.health()
println("클러스터: ${health.clusterName}")
println("상태: ${health.status}") // green, yellow, red
println("노드 수: ${health.numberOfNodes}")
println("활성 샤드: ${health.activeShards}")
println("정상 여부: ${health.isHealthy}")
```

#### 8. 고급 검색 옵션

```kotlin
val response = client.search<Product> {
    indices("products", "archived_products")

    query {
        boolQuery {
            mustQuery {
                matchQuery {
                    field = "description"
                    query = "gaming"
                }
            }
            filterQuery {
                rangeQuery {
                    field = "price"
                    gte = 100.0
                }
            }
        }
    }

    // 정렬
    sortByField("price", co.elastic.clients.elasticsearch._types.SortOrder.Asc)
    sortByScore()

    // 페이징
    size(20)
    from(40) // 처음 40개 건너뛰기

    // 소스 필터링
    includeFields("id", "name", "price")
    excludeFields("internal_metadata")

    // 타임아웃
    timeout("5s")

    // 전체 히트 수 추적
    trackTotalHits(true)
}

// 샤드 정보
val shardInfo = response.shardInfo
println("샤드 - 전체: ${shardInfo.total}, 성공: ${shardInfo.successful}")
```

#### 9. 리소스 관리

```kotlin
// use 블록으로 자동 종료
ElasticsearchClientWrapper.create().use { client ->
    val response = client.search<Product> {
        indices("products")
        query { matchAllQuery() }
    }
    println(response.totalHits)
}

// 수동 종료
val client = ElasticsearchClientWrapper.create()
try {
    // 클라이언트 사용
} finally {
    client.close()
}
```

### 응답 API

`ElasticsearchSearchResponse<T>`는 편리한 메서드를 제공합니다:

```kotlin
val response = client.search<Product> { /* ... */ }

// 히트 수
response.totalHits: Long
response.totalHitsRelation: String // "eq" 또는 "gte"

// 결과
response.hits: List<SearchHit<T>>
response.sources(): List<T>
response.firstHit(): SearchHit<T>?
response.firstSource(): T?

// 메타데이터
response.tookInMillis: Long
response.timedOut: Boolean
response.maxScore: Double?
response.shardInfo: ShardInfo

// 페이징
response.getPagingInfo(page, pageSize): PagingInfo
```

상세한 클라이언트 사용법과 고급 기능은 [ELASTICSEARCH_CLIENT_USAGE.md](ELASTICSEARCH_CLIENT_USAGE.md)를 참고하세요.

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
