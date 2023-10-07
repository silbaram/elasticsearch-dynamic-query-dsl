package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery

/**
 * gt : (선택 사항) 보다 큼.
 * gte : (선택 사항) 보다 크거나 같음.
 * lt : (선택 사항) 미만.
 * lte : (선택 사항) 보다 작거나 같음.
 * format : (선택 사항, 문자열) 쿼리의 값을 변환하는 데 사용되는 날짜 형식입니다
 */

fun rangeQuery(field: String, from: String? = null, to: String? = null): Query? {

    return if (from.isNullOrEmpty() && to.isNullOrEmpty()) {
        null
    } else {
        RangeQuery.Builder().field(field).from(from).to(to).build()._toQuery()
    }
}