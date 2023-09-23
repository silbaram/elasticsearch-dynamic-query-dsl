package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.query

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery

fun termQuery(field: String, value: String?): Query? {
    return if (value.isNullOrEmpty()) {
        null
    } else {
        TermQuery.Builder().field(field).value(value).build()._toQuery()
    }
}