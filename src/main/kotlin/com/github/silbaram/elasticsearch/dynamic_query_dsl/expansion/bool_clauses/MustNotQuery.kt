package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder

fun BoolQuery.Builder.mustNotQuery(fn: Query.Builder.() -> ObjectBuilder<Query>): BoolQuery.Builder {
    return this.mustNot(fn)
}
fun BoolQuery.Builder.mustNotQuery(vararg values: Query?): BoolQuery.Builder {
    val queries = values.asSequence().mapNotNull { it }.toList()
    return if (queries.isEmpty()) {
        this
    } else {
        this.mustNot(queries)
    }
}

fun BoolQuery.Builder.mustNotQuery(values: List<Query?>?): BoolQuery.Builder {
    val queries = values?.asSequence()?.mapNotNull { it }?.toList()
    return if (queries.isNullOrEmpty()) {
        this
    } else {
        this.mustNot(values.asSequence().mapNotNull { it }.filter { it.term().value()._get() != null }.toList())
    }
}