package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.SubQueryBuilders

/**
 * 람다를 사용하여 `mustNot` 절에 쿼리를 추가하는 통합 DSL 함수입니다.
 * 단일 쿼리 또는 `queries[...]`를 사용한 여러 쿼리를 모두 지원합니다.
 */
fun BoolQuery.Builder.mustNotQuery(fn: SubQueryBuilders.() -> Any?): BoolQuery.Builder {
    val builder = SubQueryBuilders()
    val result = builder.fn()

    // 람다의 마지막 표현식이 Query 타입이면 단일 쿼리로 간주하여 추가
    if (result is Query) {
        builder.addQuery(result)
    }

    // 빌더 내에 수집된 모든 쿼리를 mustNot 절에 추가
    builder.forEach { query ->
        this.mustNot(query)
    }
    return this
}