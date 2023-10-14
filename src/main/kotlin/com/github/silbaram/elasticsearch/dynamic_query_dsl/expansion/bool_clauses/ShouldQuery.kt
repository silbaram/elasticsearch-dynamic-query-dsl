package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder

fun BoolQuery.Builder.shouldQuery(fn: Query.Builder.() -> ObjectBuilder<Query>): BoolQuery.Builder {
    return this.should(fn)
}

fun BoolQuery.Builder.shouldQuery(vararg values: Query?): BoolQuery.Builder {
    val queries = values.asSequence().mapNotNull { it }.toList()
    return if (queries.isEmpty()) {
        this
    } else {
        this.should(queries)
    }
}

fun BoolQuery.Builder.shouldQuery(values: List<Query?>?): BoolQuery.Builder {
    val queries = values?.asSequence()?.mapNotNull { it }?.toList()
    return if (queries.isNullOrEmpty()) {
        this
    } else {
        this.should(values.asSequence().mapNotNull { it }.filter { it.term().value()._get() != null }.toList())
    }
}