# API Contract: ElasticsearchClientWrapper

**Version**: 1.0.0
**Date**: 2025-10-03

## 개요

ElasticsearchClientWrapper는 Elasticsearch 클러스터와 상호작용하는 메인 API를 제공합니다.

---

## 1. 검색 API

### 1.1 동기 검색 (SearchRequest)

**Signature**:
```kotlin
fun <T> search(
    request: SearchRequest,
    clazz: Class<T>
): ElasticsearchSearchResponse<T>
```

**Input**:
- `request: SearchRequest` - Elasticsearch 검색 요청 객체
- `clazz: Class<T>` - 응답 타입 클래스

**Output**:
- `ElasticsearchSearchResponse<T>` - 타입 안전 검색 결과

**Preconditions**:
- 클라이언트가 연결되어 있어야 함
- clazz는 Jackson 직렬화 가능한 타입이어야 함

**Postconditions**:
- 검색 결과가 T 타입으로 매핑됨
- totalHits, hits, took 정보 포함

**Errors**:
- `IOException`: 네트워크 오류
- `ElasticsearchException`: Elasticsearch 서버 오류
- `JsonProcessingException`: 역직렬화 오류

**Example**:
```kotlin
val request = SearchRequest.Builder()
    .index("products")
    .query(matchAllQuery)
    .build()

val response = client.search(request, Product::class.java)
println("Found ${response.totalHits} products")
```

---

### 1.2 동기 검색 (DSL Builder)

**Signature**:
```kotlin
fun <T> search(
    clazz: Class<T>,
    builder: SearchRequestBuilder.() -> Unit
): ElasticsearchSearchResponse<T>
```

**Input**:
- `clazz: Class<T>` - 응답 타입 클래스
- `builder: SearchRequestBuilder.() -> Unit` - DSL 빌더 람다

**Output**:
- `ElasticsearchSearchResponse<T>` - 타입 안전 검색 결과

**Preconditions**:
- 클라이언트가 연결되어 있어야 함

**Postconditions**:
- DSL 빌더로 구성된 쿼리가 실행됨

**Errors**:
- 1.1과 동일

**Example**:
```kotlin
val response = client.search(Product::class.java) {
    indices("products")
    query {
        boolQuery {
            mustQuery {
                matchQuery {
                    field = "name"
                    query = "laptop"
                }
            }
        }
    }
    size(20)
    sortByField("price", SortOrder.Desc)
}
```

---

### 1.3 비동기 검색

**Signature**:
```kotlin
suspend fun <T> searchAsync(
    request: SearchRequest,
    clazz: Class<T>
): ElasticsearchSearchResponse<T>
```

**Input**:
- `request: SearchRequest` - Elasticsearch 검색 요청 객체
- `clazz: Class<T>` - 응답 타입 클래스

**Output**:
- `ElasticsearchSearchResponse<T>` - 타입 안전 검색 결과 (suspend)

**Preconditions**:
- 코루틴 스코프 내에서 호출되어야 함
- 클라이언트가 연결되어 있어야 함

**Postconditions**:
- Dispatchers.IO에서 비블로킹 실행
- 1.1과 동일한 결과 반환

**Errors**:
- 1.1과 동일

**Example**:
```kotlin
runBlocking {
    val response = client.searchAsync(request, Product::class.java)
    println("Async search completed: ${response.totalHits} hits")
}
```

---

## 2. 인덱싱 API

### 2.1 단건 문서 인덱싱

**Signature**:
```kotlin
fun <T> index(
    index: String,
    id: String?,
    document: T
): IndexResponse
```

**Input**:
- `index: String` - 대상 인덱스명
- `id: String?` - 문서 ID (null이면 자동 생성)
- `document: T` - 인덱싱할 문서 객체

**Output**:
- `IndexResponse` - 인덱싱 결과

**Preconditions**:
- index는 비어있지 않아야 함
- document는 Jackson 직렬화 가능해야 함

**Postconditions**:
- 문서가 Elasticsearch에 저장됨
- id가 반환됨

**Errors**:
- `IOException`: 네트워크 오류
- `ElasticsearchException`: 인덱스 오류

**Example**:
```kotlin
val product = Product("P001", "Laptop", 1200.0)
val response = client.index("products", "P001", product)
println("Indexed document with ID: ${response.id()}")
```

---

### 2.2 대량 문서 인덱싱

**Signature**:
```kotlin
fun <T> bulk(
    operations: List<BulkOperation>
): BulkResponse
```

**Input**:
- `operations: List<BulkOperation>` - 대량 작업 리스트

**Output**:
- `BulkResponse` - 대량 작업 결과

**Preconditions**:
- operations가 비어있지 않아야 함

**Postconditions**:
- 모든 작업이 배치로 실행됨
- 개별 작업 성공/실패 정보 포함

**Errors**:
- `IOException`: 네트워크 오류
- `ElasticsearchException`: 대량 작업 오류

**Example**:
```kotlin
val operations = listOf(
    BulkOperation.of { it.index { idx ->
        idx.index("products").id("P001").document(product1)
    }},
    BulkOperation.of { it.index { idx ->
        idx.index("products").id("P002").document(product2)
    }}
)

val response = client.bulk(operations)
println("Bulk indexed ${operations.size} documents, errors: ${response.errors()}")
```

---

## 3. 인덱스 관리 API

### 3.1 인덱스 생성

**Signature**:
```kotlin
fun createIndex(
    name: String,
    settings: IndexSettings? = null,
    mappings: TypeMapping? = null
): CreateIndexResponse
```

**Input**:
- `name: String` - 생성할 인덱스명
- `settings: IndexSettings?` - 인덱스 설정 (선택)
- `mappings: TypeMapping?` - 매핑 정의 (선택)

**Output**:
- `CreateIndexResponse` - 생성 결과

**Preconditions**:
- name은 비어있지 않아야 함
- 동일한 이름의 인덱스가 존재하지 않아야 함

**Postconditions**:
- 인덱스가 생성됨
- acknowledged = true 반환

**Errors**:
- `ResourceAlreadyExistsException`: 인덱스가 이미 존재
- `ElasticsearchException`: 생성 오류

**Example**:
```kotlin
val response = client.createIndex(
    name = "products",
    settings = IndexSettings.Builder()
        .numberOfShards("2")
        .numberOfReplicas("1")
        .build(),
    mappings = null
)
println("Index created: ${response.acknowledged()}")
```

---

### 3.2 인덱스 삭제

**Signature**:
```kotlin
fun deleteIndex(name: String): DeleteIndexResponse
```

**Input**:
- `name: String` - 삭제할 인덱스명

**Output**:
- `DeleteIndexResponse` - 삭제 결과

**Preconditions**:
- name은 비어있지 않아야 함

**Postconditions**:
- 인덱스가 삭제됨
- acknowledged = true 반환

**Errors**:
- `ResourceNotFoundException`: 인덱스가 존재하지 않음
- `ElasticsearchException`: 삭제 오류

**Example**:
```kotlin
val response = client.deleteIndex("products")
println("Index deleted: ${response.acknowledged()}")
```

---

### 3.3 인덱스 존재 확인

**Signature**:
```kotlin
fun indexExists(name: String): Boolean
```

**Input**:
- `name: String` - 확인할 인덱스명

**Output**:
- `Boolean` - 존재 여부

**Preconditions**:
- name은 비어있지 않아야 함

**Postconditions**:
- 인덱스 존재 여부 반환

**Errors**:
- `IOException`: 네트워크 오류

**Example**:
```kotlin
if (client.indexExists("products")) {
    println("Index exists")
} else {
    client.createIndex("products")
}
```

---

## 4. 리소스 관리 API

### 4.1 클라이언트 종료

**Signature**:
```kotlin
override fun close()
```

**Input**: 없음

**Output**: 없음

**Preconditions**:
- 클라이언트가 생성되어 있어야 함

**Postconditions**:
- 연결이 종료됨
- 리소스가 해제됨

**Errors**:
- `IOException`: 종료 중 오류

**Example**:
```kotlin
client.use {
    // 작업 수행
}
// 자동으로 close() 호출됨
```

---

## 5. 팩토리 메서드

### 5.1 기본 클라이언트 생성

**Signature**:
```kotlin
companion object {
    fun create(
        host: String = "localhost",
        port: Int = 9200
    ): ElasticsearchClientWrapper
}
```

**Input**:
- `host: String` - Elasticsearch 호스트 (기본값: localhost)
- `port: Int` - Elasticsearch 포트 (기본값: 9200)

**Output**:
- `ElasticsearchClientWrapper` - 클라이언트 인스턴스

**Example**:
```kotlin
val client = ElasticsearchClientWrapper.create()
```

---

### 5.2 환경변수 기반 클라이언트 생성

**Signature**:
```kotlin
companion object {
    fun createFromEnvironment(): ElasticsearchClientWrapper
}
```

**Input**: 없음 (환경변수 사용)

**Output**:
- `ElasticsearchClientWrapper` - 클라이언트 인스턴스

**환경변수**:
- `ES_HOST`: 호스트
- `ES_PORT`: 포트
- `ES_USERNAME`: 사용자명
- `ES_PASSWORD`: 비밀번호
- `ES_USE_SSL`: SSL 사용 여부

**Example**:
```kotlin
val client = ElasticsearchClientWrapper.createFromEnvironment()
```

---

## Contract Tests

이 계약은 다음 테스트로 검증됩니다:
- `ElasticsearchClientTest.kt`: 실제 Elasticsearch 대상 통합 테스트
- `ElasticsearchClientExamplesTest.kt`: API 사용 예제 검증
