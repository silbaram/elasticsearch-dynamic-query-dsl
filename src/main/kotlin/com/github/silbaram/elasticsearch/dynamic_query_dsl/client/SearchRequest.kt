package com.github.silbaram.elasticsearch.dynamic_query_dsl.client

import co.elastic.clients.elasticsearch._types.SortOptions
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.search.SourceConfig
import co.elastic.clients.elasticsearch.core.search.SourceFilter
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query

/**
 * 검색 요청 DSL
 */
class SearchRequestBuilder {
    private var indices: List<String> = emptyList()
    private var query: Query? = null
    private var size: Int? = null
    private var from: Int? = null
    private var sorts: MutableList<SortOptions> = mutableListOf()
    private var sourceIncludes: List<String>? = null
    private var sourceExcludes: List<String>? = null
    private var trackTotalHits: Boolean = true
    private var timeout: String? = null

    /**
     * 검색할 인덱스 설정
     */
    fun indices(vararg indices: String) {
        this.indices = indices.toList()
    }

    fun indices(indices: List<String>) {
        this.indices = indices
    }

    /**
     * 쿼리 설정
     */
    fun query(fn: Query.Builder.() -> Unit) {
        this.query = com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query(fn)
    }

    fun query(query: Query) {
        this.query = query
    }

    /**
     * 결과 개수 제한
     */
    fun size(size: Int) {
        this.size = size
    }

    /**
     * 시작 위치 (페이징)
     */
    fun from(from: Int) {
        this.from = from
    }

    /**
     * 정렬 추가
     */
    fun sort(fn: SortOptions.Builder.() -> SortOptions.Builder) {
        sorts.add(SortOptions.of(fn))
    }

    /**
     * 필드별 정렬
     */
    fun sortByField(field: String, order: co.elastic.clients.elasticsearch._types.SortOrder = co.elastic.clients.elasticsearch._types.SortOrder.Asc) {
        sorts.add(SortOptions.of { s ->
            s.field { f ->
                f.field(field).order(order)
            }
        })
    }

    /**
     * 스코어 기준 정렬
     */
    fun sortByScore(order: co.elastic.clients.elasticsearch._types.SortOrder = co.elastic.clients.elasticsearch._types.SortOrder.Desc) {
        sorts.add(SortOptions.of { s ->
            s.score { sc ->
                sc.order(order)
            }
        })
    }

    /**
     * 포함할 필드 설정
     */
    fun includeFields(vararg fields: String) {
        this.sourceIncludes = fields.toList()
    }

    fun includeFields(fields: List<String>) {
        this.sourceIncludes = fields
    }

    /**
     * 제외할 필드 설정
     */
    fun excludeFields(vararg fields: String) {
        this.sourceExcludes = fields.toList()
    }

    fun excludeFields(fields: List<String>) {
        this.sourceExcludes = fields
    }

    /**
     * 전체 결과 수 추적 설정
     */
    fun trackTotalHits(track: Boolean) {
        this.trackTotalHits = track
    }

    /**
     * 타임아웃 설정
     */
    fun timeout(timeout: String) {
        this.timeout = timeout
    }

    /**
     * SearchRequest 빌드
     */
    internal fun build(): SearchRequest {
        return SearchRequest.of { s ->
            s.index(indices)

            query?.let { s.query(it) }
            size?.let { s.size(it) }
            from?.let { s.from(it) }
            timeout?.let { s.timeout(it) }

            if (sorts.isNotEmpty()) {
                s.sort(sorts)
            }

            // Source 필터 설정
            if (sourceIncludes != null || sourceExcludes != null) {
                s.source { source ->
                    source.filter { filter ->
                        sourceIncludes?.let { filter.includes(it) }
                        sourceExcludes?.let { filter.excludes(it) }
                        filter
                    }
                }
            }

            s.trackTotalHits { th ->
                th.enabled(trackTotalHits)
            }

            s
        }
    }
}

/**
 * 검색 요청 DSL 함수
 */
fun searchRequest(fn: SearchRequestBuilder.() -> Unit): SearchRequest {
    val builder = SearchRequestBuilder()
    builder.fn()
    return builder.build()
}