package com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.SubQueryBuilders

/**
 * 람다를 사용하여 `filter` 절에 쿼리를 추가하는 통합 DSL 함수입니다.
 * 단일 쿼리를 순차적으로 추가하거나 `queries[...]` 또는 `+query` 문법으로 등록할 수 있습니다.
 */
fun BoolQuery.Builder.filterQuery(
    fn: SubQueryBuilders.() -> Any?
): BoolQuery.Builder {
    val builder = SubQueryBuilders()
    val result = builder.fn()

    // 람다의 마지막 표현식이 Query 타입이고 내부에서 쿼리가 수집되지 않았다면 추가
    if (builder.size() == 0 && result is Query) {
        builder.addQuery(result)
    }

    // 빌더 내에 수집된 모든 쿼리를 filter 절에 추가
    builder.forEach { query ->
        this.filter(query)
    }
    return this
}
