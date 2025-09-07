package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query

fun matchAllQuery(
    boost: Float? = null,
    _name: String? = null
): Query {
    val builder = MatchAllQuery.Builder()

    boost?.let { builder.boost(it) }
    _name?.let { builder.queryName(it) }

    return builder.build()._toQuery()
}