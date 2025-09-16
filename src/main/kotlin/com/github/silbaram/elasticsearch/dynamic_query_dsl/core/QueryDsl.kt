package com.github.silbaram.elasticsearch.dynamic_query_dsl.core

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.MissingRequiredPropertyException

fun query(fn: Query.Builder.() -> Unit): Query {
    return Query.of { builder ->
        builder.fn()
        builder
    }
}

fun queryOrNull(fn: Query.Builder.() -> Unit): Query? {
    return try {
        Query.of { builder ->
            builder.fn()
            builder
        }
    } catch (_: MissingRequiredPropertyException) {
        null
    }
}
