package com.github.silbaram.elasticsearch.dynamic_query_dsl.core

import co.elastic.clients.elasticsearch._types.query_dsl.Query

fun query(fn: Query.Builder.() -> Unit): Query {
    return Query.of { builder ->
        builder.fn()
        builder
    }
}
