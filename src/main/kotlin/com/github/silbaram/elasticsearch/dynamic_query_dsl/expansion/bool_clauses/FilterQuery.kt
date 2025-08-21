package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder

fun BoolQuery.Builder.filterQuery(fn: Query.Builder.() -> ObjectBuilder<Query>): BoolQuery.Builder {
    return this.filter(fn)
}

fun BoolQuery.Builder.filterQuery(vararg values: Query?): BoolQuery.Builder {
    val queries = values.asSequence().mapNotNull { it }.toList()
    return if (queries.isEmpty()) {
        this
    } else {
        this.filter(queries)
    }
}

fun BoolQuery.Builder.filterQuery(values: List<Query?>?): BoolQuery.Builder {
    val queries = values?.filterNotNull() ?: emptyList()
    return if (queries.isEmpty()) {
        this
    } else {
        this.filter(queries)
    }
}