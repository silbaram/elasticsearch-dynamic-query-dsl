package com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.SubQueryBuilders

/**
 * 람다를 사용하여 `must` 절에 쿼리를 추가하는 통합 DSL 함수입니다.
 * 단일 쿼리를 순차적으로 추가하거나, 호환성을 위해 `queries[...]` 블록을 사용할 수 있습니다.
 */
fun BoolQuery.Builder.mustQuery(
    fn: SubQueryBuilders.() -> Any?
): BoolQuery.Builder {
    val builder = SubQueryBuilders()
    val result = builder.fn()

    // 람다의 마지막 표현식이 Query 타입이고 내부에서 쿼리가 수집되지 않았다면 추가
    if (builder.size() == 0 && result is Query) {
        builder.addQuery(result)
    }

    builder.forEach { query ->
        this.must(query)
    }
    return this
}
