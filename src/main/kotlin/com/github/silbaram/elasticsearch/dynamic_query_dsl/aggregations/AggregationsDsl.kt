package com.github.silbaram.elasticsearch.dynamic_query_dsl.aggregations

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.aggregationOrNull

class AggregationsDsl {
    private val aggregations = linkedMapOf<String, Aggregation>()

    fun aggregation(name: String, aggregation: Aggregation?) {
        val key = name.orNullIfBlank() ?: return
        if (aggregation != null) {
            aggregations[key] = aggregation
        }
    }

    fun aggregation(name: String, fn: Aggregation.Builder.() -> Unit) {
        aggregation(name, aggregationOrNull(fn))
    }

    fun build(): Map<String, Aggregation> = aggregations.toMap()
}

fun aggregations(fn: AggregationsDsl.() -> Unit): Map<String, Aggregation> {
    val dsl = AggregationsDsl()
    dsl.fn()
    return dsl.build()
}
