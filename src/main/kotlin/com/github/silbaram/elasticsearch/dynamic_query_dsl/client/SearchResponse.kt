package com.github.silbaram.elasticsearch.dynamic_query_dsl.client

import co.elastic.clients.elasticsearch.core.SearchResponse
import co.elastic.clients.elasticsearch.core.search.Hit
import co.elastic.clients.elasticsearch.core.search.HitsMetadata
import co.elastic.clients.elasticsearch.core.search.TotalHits
import com.fasterxml.jackson.databind.JsonNode

/**
 * 검색 응답 래퍼 클래스
 */
class ElasticsearchSearchResponse<T>(private val response: SearchResponse<T>) {

    /**
     * 전체 히트 수
     */
    val totalHits: Long
        get() = response.hits().total()?.value() ?: 0L

    /**
     * 전체 히트 관계 (정확한 수인지 추정치인지)
     */
    val totalHitsRelation: String
        get() = response.hits().total()?.relation()?.jsonValue() ?: "eq"

    /**
     * 최대 스코어
     */
    val maxScore: Double?
        get() = response.hits().maxScore()

    /**
     * 검색에 걸린 시간 (밀리초)
     */
    val tookInMillis: Long
        get() = response.took()

    /**
     * 타임아웃 여부
     */
    val timedOut: Boolean
        get() = response.timedOut()

    /**
     * 샤드 정보
     */
    val shardInfo: ShardInfo
        get() = ShardInfo(
            total = response.shards().total().toInt(),
            successful = response.shards().successful().toInt(),
            failed = response.shards().failed().toInt(),
            skipped = response.shards().skipped()?.toInt() ?: 0
        )

    /**
     * 검색 결과 히트들
     */
    val hits: List<SearchHit<T>>
        get() = response.hits().hits().map { SearchHit(it) }

    /**
     * 첫 번째 결과 반환 (없으면 null)
     */
    fun firstHit(): SearchHit<T>? = hits.firstOrNull()

    /**
     * 검색 결과 데이터만 추출
     */
    fun sources(): List<T> = hits.mapNotNull { it.source }

    /**
     * 첫 번째 결과 데이터만 반환 (없으면 null)
     */
    fun firstSource(): T? = firstHit()?.source

    /**
     * 페이징 정보
     */
    fun getPagingInfo(currentPage: Int, pageSize: Int): PagingInfo {
        val totalPages = if (pageSize > 0) {
            ((totalHits + pageSize - 1) / pageSize).toInt()
        } else 0

        return PagingInfo(
            currentPage = currentPage,
            pageSize = pageSize,
            totalHits = totalHits,
            totalPages = totalPages,
            hasNext = currentPage < totalPages,
            hasPrevious = currentPage > 1
        )
    }

    /**
     * 원본 Elasticsearch 응답 반환
     */
    fun raw(): SearchResponse<T> = response
}

/**
 * 검색 히트 래퍼 클래스
 */
class SearchHit<T>(private val hit: Hit<T>) {
    
    /**
     * 문서 ID
     */
    val id: String
        get() = hit.id() ?: ""

    /**
     * 인덱스 이름
     */
    val index: String
        get() = hit.index() ?: ""

    /**
     * 검색 스코어
     */
    val score: Double?
        get() = hit.score()

    /**
     * 문서 소스 데이터
     */
    val source: T?
        get() = hit.source()

    /**
     * 하이라이트 결과
     */
    val highlight: Map<String, List<String>>
        get() = hit.highlight()?.mapValues { entry ->
            entry.value.map { it }
        } ?: emptyMap()

    /**
     * 정렬 값들
     */
    val sort: List<String>
        get() = hit.sort()?.map { it.toString() } ?: emptyList()

    /**
     * 원본 Hit 객체 반환
     */
    fun raw(): Hit<T> = hit
}

/**
 * 샤드 정보
 */
data class ShardInfo(
    val total: Int,
    val successful: Int,
    val failed: Int,
    val skipped: Int
) {
    val isAllSuccessful: Boolean
        get() = failed == 0 && successful == total
}

/**
 * 페이징 정보
 */
data class PagingInfo(
    val currentPage: Int,
    val pageSize: Int,
    val totalHits: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    val offset: Int
        get() = (currentPage - 1) * pageSize
}