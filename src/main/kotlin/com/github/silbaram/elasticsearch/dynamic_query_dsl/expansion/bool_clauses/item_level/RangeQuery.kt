package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery
import co.elastic.clients.json.JsonData

/**
 * gt : (선택 사항) 보다 큼.
 * gte : (선택 사항) 보다 크거나 같음.
 * lt : (선택 사항) 미만.
 * lte : (선택 사항) 보다 작거나 같음.
 * format : (선택 사항, 문자열) 쿼리의 값을 변환하는 데 사용되는 날짜 형식입니다
 */

fun rangeQuery(
    field: String,
    from: String? = null,
    to: String? = null,
    gt: Any? = null,
    lt: Any? = null,
    gte: Any? = null,
    lte: Any? = null
): Query? {
    if (from.isNullOrEmpty() && to.isNullOrEmpty() && gt == null && lt == null && gte == null && lte == null) {
        return null
    }

    val builder = RangeQuery.Builder()

    // Set field via reflection to remain compatible with client versions
    builder.javaClass.methods
        .firstOrNull { it.name == "field" && it.parameterTypes.size == 1 }
        ?.invoke(builder, field)
        ?: builder.javaClass.getDeclaredField("field").apply {
            isAccessible = true
            set(builder, field)
        }

    fun invokeIfPresent(name: String, value: Any) {
        builder.javaClass.methods
            .firstOrNull { it.name == name && it.parameterTypes.size == 1 }
            ?.invoke(builder, value)
    }

    from?.let { invokeIfPresent("from", JsonData.of(it)) }
    to?.let { invokeIfPresent("to", JsonData.of(it)) }
    gt?.let { invokeIfPresent("gt", JsonData.of(it)) }
    lt?.let { invokeIfPresent("lt", JsonData.of(it)) }
    gte?.let { invokeIfPresent("gte", JsonData.of(it)) }
    lte?.let { invokeIfPresent("lte", JsonData.of(it)) }

    return builder.build()._toQuery()
}