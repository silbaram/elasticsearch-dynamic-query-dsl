package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query

fun existsQuery(
    field: String?,
    boost: Float? = null,
    _name: String? = null
): Query? {
    return if (field.isNullOrEmpty()) {
        null
    } else {
        val builder = ExistsQuery.Builder()
            .field(field)

        boost?.let { builder.boost(it) }
        _name?.let { builder.queryName(it) }

        builder.build()._toQuery()
    }
}