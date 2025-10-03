package com.github.silbaram.elasticsearch.dynamic_query_dsl.client

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.BulkRequest
import co.elastic.clients.elasticsearch.core.IndexRequest
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest
import co.elastic.clients.elasticsearch.indices.ExistsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.StringReader
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Elasticsearch 클라이언트 래퍼 클래스
 */
class ElasticsearchClientWrapper(
    private val config: ElasticsearchClientConfig
) : Closeable {

    private val client: ElasticsearchClient = config.createClient()

    /**
     * 검색 실행 (동기)
     */
    fun <T> search(
        request: SearchRequest,
        clazz: Class<T>
    ): ElasticsearchSearchResponse<T> {
        val response = client.search(request, clazz)
        return ElasticsearchSearchResponse(response)
    }

    /**
     * 검색 실행 (비동기)
     */
    suspend fun <T> searchAsync(
        request: SearchRequest,
        clazz: Class<T>
    ): ElasticsearchSearchResponse<T> = withContext(Dispatchers.IO) {
        search(request, clazz)
    }

    /**
     * DSL을 사용한 검색
     */
    fun <T> search(
        clazz: Class<T>,
        fn: SearchRequestBuilder.() -> Unit
    ): ElasticsearchSearchResponse<T> {
        val request = searchRequest(fn)
        return search(request, clazz)
    }

    /**
     * DSL을 사용한 비동기 검색
     */
    suspend fun <T> searchAsync(
        clazz: Class<T>,
        fn: SearchRequestBuilder.() -> Unit
    ): ElasticsearchSearchResponse<T> = withContext(Dispatchers.IO) {
        search(clazz, fn)
    }

    /**
     * 간단한 쿼리 검색
     */
    fun <T> search(
        indices: List<String>,
        query: Query,
        clazz: Class<T>,
        size: Int = 10,
        from: Int = 0
    ): ElasticsearchSearchResponse<T> {
        return search(clazz) {
            indices(indices)
            query(query)
            size(size)
            from(from)
        }
    }

    /**
     * 문서 인덱싱
     */
    fun <T> index(
        index: String,
        document: T,
        id: String? = null
    ): String {
        val request = IndexRequest.of<T> { i ->
            i.index(index)
                .document(document)
            id?.let { i.id(it) }
            i
        }
        val response = client.index(request)
        return response.id()
    }

    /**
     * 문서 인덱싱 (비동기)
     */
    suspend fun <T> indexAsync(
        index: String,
        document: T,
        id: String? = null
    ): String = withContext(Dispatchers.IO) {
        index(index, document, id)
    }

    /**
     * 대량 문서 인덱싱
     */
    fun <T> bulkIndex(
        index: String,
        documents: List<T>,
        idExtractor: ((T) -> String)? = null
    ): BulkIndexResult {
        val bulkRequest = BulkRequest.Builder()

        documents.forEach { document ->
            bulkRequest.operations { op ->
                op.index { idx ->
                    idx.index(index)
                        .document(document)
                    idExtractor?.let { extractor ->
                        idx.id(extractor(document))
                    }
                    idx
                }
            }
        }

        val response = client.bulk(bulkRequest.build())
        
        return BulkIndexResult(
            took = response.took(),
            hasErrors = response.errors(),
            items = response.items().map { item ->
                BulkIndexItem(
                    id = item.id(),
                    index = item.index(),
                    status = item.status(),
                    error = item.error()?.reason()
                )
            }
        )
    }

    /**
     * 대량 문서 인덱싱 (비동기)
     */
    suspend fun <T> bulkIndexAsync(
        index: String,
        documents: List<T>,
        idExtractor: ((T) -> String)? = null
    ): BulkIndexResult = withContext(Dispatchers.IO) {
        bulkIndex(index, documents, idExtractor)
    }

    /**
     * 인덱스 존재 확인
     */
    fun indexExists(index: String): Boolean {
        return client.indices().exists(ExistsRequest.of { e -> e.index(index) }).value()
    }

    /**
     * 인덱스 생성
     */
    fun createIndex(
        index: String,
        mappings: Map<String, Any>? = null,
        settings: Map<String, Any>? = null
    ): Boolean {
        val request = buildCreateIndexRequest(index, mappings, settings)
        val response = client.indices().create(request)
        return response.acknowledged()
    }

    /**
     * 인덱스 삭제
     */
    fun deleteIndex(index: String): Boolean {
        val request = DeleteIndexRequest.of { d -> d.index(index) }
        val response = client.indices().delete(request)
        return response.acknowledged()
    }

    /**
     * 클러스터 헬스 체크
     */
    fun health(): ClusterHealth {
        val response = client.cluster().health()
        return ClusterHealth(
            clusterName = response.clusterName(),
            status = response.status().jsonValue(),
            numberOfNodes = response.numberOfNodes(),
            numberOfDataNodes = response.numberOfDataNodes(),
            activePrimaryShards = response.activePrimaryShards(),
            activeShards = response.activeShards(),
            relocatingShards = response.relocatingShards(),
            initializingShards = response.initializingShards(),
            unassignedShards = response.unassignedShards()
        )
    }

    override fun close() {
        try {
            // Elasticsearch 클라이언트의 내부 RestClient를 안전하게 종료
            client._transport().close()
        } catch (e: Exception) {
            // 로깅 또는 예외 처리
        }
    }

    companion object {
        /**
         * 기본 설정으로 클라이언트 생성
         */
        fun create(): ElasticsearchClientWrapper {
            return ElasticsearchClientWrapper(ElasticsearchClientConfig.local())
        }

        /**
         * 환경변수 설정으로 클라이언트 생성
         */
        fun createFromEnvironment(): ElasticsearchClientWrapper {
            return ElasticsearchClientWrapper(ElasticsearchClientConfig.fromEnvironment())
        }

        /**
         * 커스텀 설정으로 클라이언트 생성
         */
        fun create(config: ElasticsearchClientConfig): ElasticsearchClientWrapper {
            return ElasticsearchClientWrapper(config)
        }
    }
}

private fun Map<String, Any>.ensurePropertiesEnvelope(): Map<String, Any> {
    return if (this.keys.any { it == "properties" }) {
        this
    } else {
        mapOf("properties" to this)
    }
}

private val jacksonObjectMapper = ObjectMapper()

private fun Map<String, Any>.toJsonReader(): StringReader {
    val json = jacksonObjectMapper.writeValueAsString(this)
    return StringReader(json)
}

internal fun buildCreateIndexRequest(
    index: String,
    mappings: Map<String, Any>?,
    settings: Map<String, Any>?
): CreateIndexRequest {
    return CreateIndexRequest.Builder().apply {
        index(index)

        mappings?.let { mappingMap ->
            val payload = mappingMap.ensurePropertiesEnvelope()
            mappings { builder -> builder.withJson(payload.toJsonReader()) }
        }

        settings?.let { settingsMap ->
            settings { builder -> builder.withJson(settingsMap.toJsonReader()) }
        }
    }.build()
}

/**
 * 대량 인덱싱 결과
 */
data class BulkIndexResult(
    val took: Long,
    val hasErrors: Boolean,
    val items: List<BulkIndexItem>
) {
    val successfulItems: List<BulkIndexItem>
        get() = items.filter { it.error == null }
    
    val failedItems: List<BulkIndexItem>
        get() = items.filter { it.error != null }
    
    val successCount: Int
        get() = successfulItems.size
    
    val failureCount: Int
        get() = failedItems.size
}

/**
 * 대량 인덱싱 개별 아이템 결과
 */
data class BulkIndexItem(
    val id: String?,
    val index: String?,
    val status: Int,
    val error: String?
)

/**
 * 클러스터 헬스 정보
 */
data class ClusterHealth(
    val clusterName: String,
    val status: String,
    val numberOfNodes: Int,
    val numberOfDataNodes: Int,
    val activePrimaryShards: Int,
    val activeShards: Int,
    val relocatingShards: Int,
    val initializingShards: Int,
    val unassignedShards: Int
) {
    val isHealthy: Boolean
        get() = status in listOf("green", "yellow")
}

/**
 * Kotlin 확장 함수들
 */
inline fun <reified T> ElasticsearchClientWrapper.search(
    request: SearchRequest
): ElasticsearchSearchResponse<T> = search(request, T::class.java)

inline fun <reified T> ElasticsearchClientWrapper.search(
    noinline fn: SearchRequestBuilder.() -> Unit
): ElasticsearchSearchResponse<T> = search(T::class.java, fn)

inline fun <reified T> ElasticsearchClientWrapper.search(
    indices: List<String>,
    query: Query,
    size: Int = 10,
    from: Int = 0
): ElasticsearchSearchResponse<T> = search(indices, query, T::class.java, size, from)

suspend inline fun <reified T> ElasticsearchClientWrapper.searchAsync(
    request: SearchRequest
): ElasticsearchSearchResponse<T> = searchAsync(request, T::class.java)

suspend inline fun <reified T> ElasticsearchClientWrapper.searchAsync(
    noinline fn: SearchRequestBuilder.() -> Unit
): ElasticsearchSearchResponse<T> = searchAsync(T::class.java, fn)
