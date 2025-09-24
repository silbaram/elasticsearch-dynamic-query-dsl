package com.github.silbaram.elasticsearch.dynamic_query_dsl.core

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation
import co.elastic.clients.util.MissingRequiredPropertyException

fun aggregation(fn: Aggregation.Builder.() -> Unit): Aggregation {
    requireNotNull(fn) { "Aggregation builder lambda must not be null" }
    return Aggregation.of { builder ->
        builder.apply(fn)
        builder
    }
}

fun aggregationOrNull(fn: Aggregation.Builder.() -> Unit): Aggregation? {
    requireNotNull(fn) { "Aggregation builder lambda must not be null" }
    return try {
        aggregation(fn)
    } catch (_: MissingRequiredPropertyException) {
        null
    }
}
