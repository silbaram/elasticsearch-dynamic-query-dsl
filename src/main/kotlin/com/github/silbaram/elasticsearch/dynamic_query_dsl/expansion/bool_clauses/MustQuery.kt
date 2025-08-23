package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query

/**
 * 람다를 사용하여 `must` 절에 쿼리를 추가하는 통합 DSL 함수입니다.
 * 단일 쿼리 또는 `queries[...]`를 사용한 여러 쿼리를 모두 지원합니다.
 */
fun BoolQuery.Builder.mustQuery(fn: MustQueryBuilder.() -> Any?): BoolQuery.Builder {
    val builder = MustQueryBuilder()
    val result = builder.fn() // 람다 실행

    // 람다의 마지막 표현식이 Query 타입이면 단일 쿼리로 간주하여 추가
    if (result is Query) {
        builder.collectedQueries.add(result)
    }

    // 빌더 내에 수집된 모든 쿼리를 must 절에 추가
    builder.collectedQueries.forEach { query ->
        this.must(query)
    }
    return this
}

/**
 * 쿼리를 수집하는 헬퍼 클래스.
 */
class MustQueryBuilder {
    // 수집된 쿼리를 저장하는 내부 리스트
    internal val collectedQueries = mutableListOf<Query>()

    /**
     * `queries[...]` 구문을 사용하기 위한 프로퍼티.
     */
    val queries: MustQueryBuilder
        get() = this

    /**
     * `[...]` 구문을 오버로딩합니다.
     */
    operator fun get(vararg queries: Query?) {
        this.collectedQueries.addAll(queries.filterNotNull())
    }
}