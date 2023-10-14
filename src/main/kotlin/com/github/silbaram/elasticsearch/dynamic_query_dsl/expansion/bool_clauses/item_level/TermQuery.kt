package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery

fun termQuery(field: String, value: String?, boost: Float? = null): Query? {
    return if (value.isNullOrEmpty()) {
        null
    } else {
        TermQuery.Builder().field(field).value(value).boost(boost).build()._toQuery()
    }
}