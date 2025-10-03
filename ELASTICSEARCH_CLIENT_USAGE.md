# Elasticsearch 클라이언트 사용 가이드

이 가이드는 elasticsearch-dynamic-query-dsl 라이브러리에서 제공하는 Elasticsearch 클라이언트 기능을 설명합니다.

## 개요

이 라이브러리는 다음과 같은 기능을 제공합니다:
- **타입 안전한 DSL**: Kotlin DSL을 사용한 Elasticsearch 쿼리 작성
- **클라이언트 래퍼**: Elasticsearch Java Client를 기반으로 한 사용하기 쉬운 클라이언트
- **비동기 지원**: 코루틴을 사용한 비동기 검색
- **검색 응답 래퍼**: 편리한 응답 처리와 페이징 지원
- **인덱스 관리**: 인덱스 생성, 삭제, 문서 인덱싱

## 의존성 추가

```kotlin
dependencies {
    implementation("io.github.silbaram:elasticsearch-dynamic-query-dsl:1.0.0-SNAPSHOT")
    implementation("co.elastic.clients:elasticsearch-java:8.14.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}
```

## 기본 설정

### 1. 클라이언트 생성

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.client.*

// 로컬 Elasticsearch (기본 설정)
val client = ElasticsearchClientWrapper.create()

// 환경변수에서 설정 로드
val client = ElasticsearchClientWrapper.createFromEnvironment()

// 커스텀 설정
val client = ElasticsearchClientWrapper.create(
    ElasticsearchClientConfig(
        hosts = listOf("elasticsearch-node1:9200", "elasticsearch-node2:9200"),
        protocol = "https",
        username = "elastic",
        password = "changeme",
        enableSsl = true
    )
)
```

### 2. 환경변수 설정

```bash
ELASTICSEARCH_HOSTS=es1.example.com:9200,es2.example.com:9200
ELASTICSEARCH_PROTOCOL=https
ELASTICSEARCH_USERNAME=elastic
ELASTICSEARCH_PASSWORD=changeme
ELASTICSEARCH_SSL_ENABLED=true
ELASTICSEARCH_CONNECT_TIMEOUT=5000
ELASTICSEARCH_SOCKET_TIMEOUT=60000
```

## 검색 기능

### 기본 검색

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val description: String
)

// 기본 검색
val response = client.search<Product> {
    indices("products")
    query {
        matchQuery {
            field = "name"
            query = "laptop"
        }
    }
    size(10)
    from(0)
}

println("검색 결과: ${response.totalHits}개")
response.hits.forEach { hit ->
    println("제품: ${hit.source?.name}, 점수: ${hit.score}")
}
```

### 복합 쿼리

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.rangeQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.*

val complexResponse = client.search<Product> {
    indices("products")
    query {
        boolQuery {
            mustQuery {
                termQuery {
                    field = "category"
                    value = "electronics"
                }
            }
            mustQuery {
                rangeQuery {
                    field = "price"
                    gte = 100.0
                    lte = 1000.0
                }
            }
            shouldQuery {
                matchQuery {
                    field = "description"
                    query = "high quality"
                }
            }
        }
    }
    sortByField("price", co.elastic.clients.elasticsearch._types.SortOrder.Asc)
    size(20)
}
```

### 정렬과 페이징

```kotlin
val pagedResponse = client.search<Product> {
    indices("products")
    query {
        matchQuery {
            field = "category"
            query = "smartphone"
        }
    }
    
    // 정렬
    sortByField("price", co.elastic.clients.elasticsearch._types.SortOrder.Desc)
    sortByScore()  // 스코어 기준 정렬도 가능
    
    // 페이징
    size(10)
    from(20)  // 3페이지 (0-based)
    
    // 필드 필터링
    includeFields("id", "name", "price")
    excludeFields("description")
}

// 페이징 정보 확인
val pagingInfo = pagedResponse.getPagingInfo(currentPage = 3, pageSize = 10)
println("페이지: ${pagingInfo.currentPage}/${pagingInfo.totalPages}")
println("다음 페이지: ${pagingInfo.hasNext}")
```

### 비동기 검색

```kotlin
import kotlinx.coroutines.runBlocking

runBlocking {
    val asyncResponse = client.searchAsync<Product> {
        indices("products")
        query {
            termQuery {
                field = "inStock"
                value = "true"
            }
        }
    }
    
    println("비동기 검색 완료: ${asyncResponse.totalHits}개 상품")
}
```

## 문서 인덱싱

### 단일 문서 인덱싱

```kotlin
val product = Product(
    id = "1",
    name = "MacBook Pro",
    category = "laptop", 
    price = 2499.99,
    description = "Apple MacBook Pro with M2 chip"
)

// 문서 인덱싱 (ID 자동 생성)
val docId = client.index("products", product)

// 문서 인덱싱 (ID 지정)
val docId = client.index("products", product, product.id)

println("문서 인덱싱 완료: $docId")
```

### 대량 문서 인덱싱

```kotlin
val products = listOf(
    Product("1", "MacBook Pro", "laptop", 2499.99, "Apple laptop"),
    Product("2", "iPad Air", "tablet", 599.99, "Apple tablet"),
    Product("3", "iPhone 15", "smartphone", 999.99, "Apple phone")
)

val bulkResult = client.bulkIndex("products", products) { it.id }

println("성공: ${bulkResult.successCount}, 실패: ${bulkResult.failureCount}")

// 실패한 항목 확인
bulkResult.failedItems.forEach { item ->
    println("실패: ${item.id} - ${item.error}")
}
```

### 비동기 인덱싱

```kotlin
runBlocking {
    val docId = client.indexAsync("products", product)
    val bulkResult = client.bulkIndexAsync("products", products) { it.id }
}
```

## 인덱스 관리

### 인덱스 존재 확인 및 생성

```kotlin
val indexName = "products"

// 인덱스 존재 확인
if (!client.indexExists(indexName)) {
    
    // 인덱스 생성 (기본)
    client.createIndex(indexName)
    
    // 매핑과 설정을 포함한 인덱스 생성
    client.createIndex(
        index = indexName,
        mappings = mapOf(
            "id" to mapOf("type" to "keyword"),
            "name" to mapOf("type" to "text", "analyzer" to "standard"),
            "category" to mapOf("type" to "keyword"),
            "price" to mapOf("type" to "double"),
            "description" to mapOf("type" to "text")
        ),
        settings = mapOf(
            "number_of_shards" to 1,
            "number_of_replicas" to 0
        )
    )
}
```

### 클러스터 상태 확인

```kotlin
val health = client.health()
println("클러스터 상태: ${health.status}")
println("노드 수: ${health.numberOfNodes}")
println("활성 샤드: ${health.activeShards}")

if (health.isHealthy) {
    println("클러스터가 정상 상태입니다.")
} else {
    println("클러스터 상태를 확인하세요!")
}
```

### 인덱스 삭제

```kotlin
// 주의: 데이터가 모두 삭제됩니다!
client.deleteIndex("old-products")
```

## 검색 응답 활용

### 기본 응답 정보

```kotlin
val response = client.search<Product> { /* ... */ }

// 기본 정보
println("전체 결과 수: ${response.totalHits}")
println("최대 스코어: ${response.maxScore}")
println("검색 시간: ${response.tookInMillis}ms")
println("타임아웃 여부: ${response.timedOut}")

// 샤드 정보
val shardInfo = response.shardInfo
println("전체 샤드: ${shardInfo.total}")
println("성공한 샤드: ${shardInfo.successful}")
println("실패한 샤드: ${shardInfo.failed}")
```

### 검색 결과 처리

```kotlin
// 모든 결과
response.hits.forEach { hit ->
    println("문서 ID: ${hit.id}")
    println("인덱스: ${hit.index}")
    println("스코어: ${hit.score}")
    println("데이터: ${hit.source}")
}

// 첫 번째 결과만
val firstResult = response.firstHit()?.source
val firstSource = response.firstSource()

// 소스 데이터만 추출
val sources = response.sources()
```

## 에러 처리

```kotlin
try {
    val response = client.search<Product> { /* ... */ }
    
    // 검색은 성공했지만 결과가 없는 경우
    if (response.totalHits == 0L) {
        println("검색 결과가 없습니다.")
    }
    
} catch (e: Exception) {
    when (e) {
        is co.elastic.clients.elasticsearch._types.ElasticsearchException -> {
            println("Elasticsearch 오류: ${e.error().reason()}")
        }
        else -> {
            println("네트워크 또는 기타 오류: ${e.message}")
        }
    }
}
```

## 리소스 정리

```kotlin
// 클라이언트 사용 완료 후 반드시 닫기
client.close()

// try-with-resources 패턴 사용 권장
ElasticsearchClientWrapper.create().use { client ->
    val response = client.search<Product> { /* ... */ }
    // 자동으로 client.close() 호출됨
}
```

## 환경별 설정 예제

### 개발 환경

```kotlin
val devClient = ElasticsearchClientWrapper.create(
    ElasticsearchClientConfig.local()
)
```

### 스테이징/운영 환경

```kotlin
val prodClient = ElasticsearchClientWrapper.create(
    ElasticsearchClientConfig.production(
        hosts = listOf("es-cluster-1:9200", "es-cluster-2:9200"),
        username = System.getenv("ES_USERNAME"),
        password = System.getenv("ES_PASSWORD")
    )
)
```

### Docker Compose 환경

```yaml
# docker-compose.yml
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.14.3
    ports:
      - "9200:9200"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms1g -Xmx1g"
```

```kotlin
// Docker 환경용 클라이언트
val dockerClient = ElasticsearchClientWrapper.create(
    ElasticsearchClientConfig(
        hosts = listOf("localhost:9200"),
        protocol = "http",
        enableSsl = false
    )
)
```

## 주의사항

1. **클라이언트 종료**: 사용 후 반드시 `client.close()`를 호출하세요.
2. **타입 안전성**: 제네릭 타입을 명시하여 타입 안전성을 보장하세요.
3. **인덱스 설정**: 운영 환경에서는 적절한 샤드와 복제본 설정이 필요합니다.
4. **보안**: 운영 환경에서는 반드시 SSL/TLS와 인증을 사용하세요.
5. **성능**: 대량 데이터 처리시 bulk API를 사용하세요.

## 더 많은 예제

더 상세한 예제는 다음 파일들을 참고하세요:
- `ElasticsearchClientExamplesTest.kt`: 다양한 사용 예제 (테스트 형태)
- `ElasticsearchClientTest.kt`: 실제 클러스터 연동 테스트 예제  
- 기존 쿼리 DSL 문서: `README.md`