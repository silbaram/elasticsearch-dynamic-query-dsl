package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery

fun termQuery(field: String, value: String?, boost: Float? = null, _name: String? = null): Query? {
    return if (value.isNullOrEmpty()) {
        null
    } else {
        val builder = TermQuery.Builder()
            .field(field)
            .value(value)

        boost?.let { builder.boost(it) }
        _name?.let { builder.queryName(it) }

        builder.build()._toQuery()
    }
}