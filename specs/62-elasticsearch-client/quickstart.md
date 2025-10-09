# Quickstart: Elasticsearch 클라이언트

**Feature**: Elasticsearch 클라이언트
**Date**: 2025-10-03

## 5분 안에 시작하기

이 가이드는 Elasticsearch 클라이언트의 핵심 기능을 빠르게 실습할 수 있도록 합니다.

---

## 1. 사전 준비

### 1.1 의존성 추가

`build.gradle.kts`에 라이브러리를 추가하세요. **공식 elasticsearch-java 클라이언트는 이미 포함되어 있습니다.**

```kotlin
dependencies {
    implementation("io.github.silbaram:elasticsearch-dynamic-query-dsl:1.0.0-SNAPSHOT")
    // elasticsearch-java, jackson, coroutines는 전이 의존성으로 자동 추가됨
}
```

### 1.2 Elasticsearch 실행

Docker로 로컬 Elasticsearch를 실행하세요:

```bash
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  docker.elastic.co/elasticsearch/elasticsearch:8.14.3
```

### 1.3 연결 확인

```bash
curl http://localhost:9200
```

응답 예시:
```json
{
  "name" : "...",
  "cluster_name" : "docker-cluster",
  "version" : { "number" : "8.14.3" }
}
```

---

## 2. 첫 번째 검색

### 2.1 데이터 클래스 정의

```kotlin
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Double
)
```

### 2.2 클라이언트 생성 및 검색

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.client.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery

fun main() {
    // 클라이언트 생성 (공식 elasticsearch-java 클라이언트 래핑)
    val client = ElasticsearchClientWrapper.create(
        host = "localhost",
        port = 9200
    )

    client.use {
        // 인덱스 생성
        if (!it.indexExists("products")) {
            it.createIndex("products")
        }

        // 샘플 데이터 인덱싱
        val product = Product(
            id = "P001",
            name = "Gaming Laptop",
            category = "Electronics",
            price = 1500.0
        )
        it.index("products", "P001", product)

        // 1초 대기 (인덱싱 완료)
        Thread.sleep(1000)

        // DSL로 검색 (공식 클라이언트를 쉽게 사용)
        val response = it.search(Product::class.java) {
            indices("products")
            query {
                matchQuery {
                    field = "name"
                    query = "laptop"
                }
            }
        }

        // 결과 출력
        println("Found ${response.totalHits} products")
        response.hits.forEach { hit ->
            println("${hit.source?.name}: $${hit.source?.price}")
        }
    }
}
```

**출력**:
```
Found 1 products
Gaming Laptop: $1500.0
```

---

## 3. DSL 빌더로 복잡한 쿼리

### 3.1 Bool 쿼리 사용

```kotlin
val response = client.search(Product::class.java) {
    indices("products")
    query {
        boolQuery {
            mustQuery {
                matchQuery {
                    field = "category"
                    query = "electronics"
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
    size(10)
    sortByField("price", co.elastic.clients.elasticsearch._types.SortOrder.Desc)
}

println("Found ${response.totalHits} products in price range")
```

### 3.2 페이징 처리

```kotlin
fun searchPage(page: Int, pageSize: Int = 10) {
    val response = client.search(Product::class.java) {
        indices("products")
        query {
            matchAllQuery()
        }
        size(pageSize)
        from(page * pageSize)
    }

    println("Page $page:")
    response.hits.forEach { println("  - ${it.source?.name}") }
}

// 첫 페이지
searchPage(0)

// 두 번째 페이지
searchPage(1)
```

---

## 4. 비동기 검색

### 4.1 코루틴 사용

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val client = ElasticsearchClientWrapper.create()

    client.use {
        // 병렬로 여러 검색 실행
        val laptops = async {
            it.searchAsync(Product::class.java) {
                indices("products")
                query {
                    matchQuery {
                        field = "category"
                        query = "laptop"
                    }
                }
            }
        }

        val phones = async {
            it.searchAsync(Product::class.java) {
                indices("products")
                query {
                    matchQuery {
                        field = "category"
                        query = "phone"
                    }
                }
            }
        }

        // 결과 대기
        println("Laptops: ${laptops.await().totalHits}")
        println("Phones: ${phones.await().totalHits}")
    }
}
```

---

## 5. 대량 인덱싱

### 5.1 Bulk API 사용

```kotlin
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation

fun bulkIndexProducts(products: List<Product>) {
    val operations = products.map { product ->
        BulkOperation.of { op ->
            op.index { idx ->
                idx.index("products")
                   .id(product.id)
                   .document(product)
            }
        }
    }

    val response = client.bulk(operations)

    if (response.errors()) {
        println("Bulk indexing had errors")
        response.items().filter { it.error() != null }.forEach {
            println("Error: ${it.error()?.reason()}")
        }
    } else {
        println("Successfully indexed ${products.size} products")
    }
}

// 사용 예시
val products = listOf(
    Product("P001", "Laptop", "Electronics", 1200.0),
    Product("P002", "Mouse", "Accessories", 25.0),
    Product("P003", "Keyboard", "Accessories", 80.0)
)

bulkIndexProducts(products)
```

---

## 6. 인덱스 관리

### 6.1 인덱스 생성 및 삭제

```kotlin
// 인덱스 생성 (설정 포함)
client.createIndex(
    name = "products",
    settings = co.elastic.clients.elasticsearch.indices.IndexSettings.Builder()
        .numberOfShards("2")
        .numberOfReplicas("1")
        .build()
)

// 존재 확인
if (client.indexExists("products")) {
    println("Index exists")
}

// 인덱스 삭제
client.deleteIndex("products")
```

---

## 7. 환경변수 설정

### 7.1 설정 파일

`.env` 파일 또는 시스템 환경변수:

```bash
ES_HOST=localhost
ES_PORT=9200
ES_USERNAME=elastic
ES_PASSWORD=changeme
ES_USE_SSL=false
```

### 7.2 환경변수로 클라이언트 생성

```kotlin
val client = ElasticsearchClientWrapper.createFromEnvironment()
```

---

## 8. 에러 처리

### 8.1 연결 오류 처리

```kotlin
try {
    val client = ElasticsearchClientWrapper.create(host = "invalid-host")
    client.use {
        it.search(Product::class.java) {
            indices("products")
        }
    }
} catch (e: IOException) {
    println("Connection error: ${e.message}")
} catch (e: ElasticsearchException) {
    println("Elasticsearch error: ${e.message}")
}
```

### 8.2 인덱스 없음 처리

```kotlin
try {
    val response = client.search(Product::class.java) {
        indices("nonexistent_index")
    }
} catch (e: ElasticsearchException) {
    if (e.status() == 404) {
        println("Index not found")
    }
}
```

---

## 9. 완전한 예제

### 9.1 전자상거래 제품 검색

```kotlin
import com.github.silbaram.elasticsearch.dynamic_query_dsl.client.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.rangeQuery

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val inStock: Boolean
)

fun main() {
    // elasticsearch-java 클라이언트를 쉽게 사용할 수 있는 래퍼
    val client = ElasticsearchClientWrapper.create()

    client.use { es ->
        // 1. 인덱스 설정
        if (!es.indexExists("products")) {
            es.createIndex("products")
        }

        // 2. 샘플 데이터 인덱싱
        val products = listOf(
            Product("P001", "Gaming Laptop", "Electronics", 1500.0, true),
            Product("P002", "Wireless Mouse", "Accessories", 25.0, true),
            Product("P003", "Mechanical Keyboard", "Accessories", 120.0, false),
            Product("P004", "4K Monitor", "Electronics", 450.0, true)
        )

        products.forEach { product ->
            es.index("products", product.id, product)
        }

        Thread.sleep(1000) // 인덱싱 완료 대기

        // 3. 검색: 재고 있는 전자제품, 가격 100~2000
        val response = es.search(Product::class.java) {
            indices("products")
            query {
                boolQuery {
                    mustQuery {
                        matchQuery {
                            field = "category"
                            query = "electronics"
                        }
                    }
                    mustQuery {
                        rangeQuery {
                            field = "price"
                            gte = 100.0
                            lte = 2000.0
                        }
                    }
                    filterQuery {
                        termQuery {
                            field = "inStock"
                            value = true
                        }
                    }
                }
            }
            size(10)
            sortByField("price", co.elastic.clients.elasticsearch._types.SortOrder.Asc)
        }

        // 4. 결과 출력
        println("=== 재고 있는 전자제품 (가격순) ===")
        println("총 ${response.totalHits}건")
        response.hits.forEach { hit ->
            val product = hit.source
            println("${product?.name}: $${product?.price} (재고: ${product?.inStock})")
        }

        // 5. 정리
        es.deleteIndex("products")
        println("\n인덱스 삭제 완료")
    }
}
```

**출력**:
```
=== 재고 있는 전자제품 (가격순) ===
총 2건
4K Monitor: $450.0 (재고: true)
Gaming Laptop: $1500.0 (재고: true)

인덱스 삭제 완료
```

---

## 핵심 포인트

### 공식 클라이언트를 더 쉽게
- **elasticsearch-java** 공식 클라이언트는 이미 포함되어 있습니다
- DSL 빌더 패턴으로 **타입 안전**하게 쿼리 작성
- **자동 정제**: null/빈 값은 자동으로 생략
- **코루틴 지원**: 비동기 검색 간편화

### 장점
1. ✅ 복잡한 elasticsearch-java API를 Kotlin DSL로 간소화
2. ✅ 타입 안전성으로 컴파일 타임 에러 방지
3. ✅ 환경변수 기반 설정으로 유연성 향상
4. ✅ 코루틴 네이티브 지원

---

## 다음 단계

### 학습 자료
- [데이터 모델](./data-model.md): 엔티티 및 관계 이해
- [API 계약](./contracts/client-api.md): 전체 API 레퍼런스
- [조사 문서](./research.md): 기술 결정 배경

### 고급 기능
- Scroll API로 대용량 데이터 검색
- Point-in-Time (PIT) 검색
- 집계(Aggregations)와 통합
- 커스텀 분석기 설정

### 테스트
- `ElasticsearchClientTest.kt`: 통합 테스트 예제
- `ElasticsearchClientExamplesTest.kt`: 사용 패턴 예제

---

## 문제 해결

### Q1: "Connection refused" 에러
**A**: Elasticsearch가 실행 중인지 확인하세요.
```bash
docker ps | grep elasticsearch
```

### Q2: 검색 결과가 0건
**A**: 인덱싱 후 1초 대기하세요 (refresh 시간).
```kotlin
Thread.sleep(1000)
```

### Q3: SSL 연결 오류
**A**: `useSsl = true` 설정 및 인증서 확인.
```kotlin
val config = ElasticsearchClientConfig(
    host = "localhost",
    port = 9243,
    useSsl = true
)
```

### Q4: 메모리 부족
**A**: `client.use { }` 블록으로 자동 리소스 해제.
```kotlin
ElasticsearchClientWrapper.create().use { client ->
    // 작업
} // 자동 close()
```
