package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query

fun existsQuery(field: String?): Query? {
    return if (field.isNullOrEmpty()) {
        null
    } else {
        ExistsQuery.Builder().field(field).build()._toQuery()
    }
}