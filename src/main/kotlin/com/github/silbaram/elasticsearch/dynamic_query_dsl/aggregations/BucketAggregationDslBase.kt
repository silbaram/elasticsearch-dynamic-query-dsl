package com.github.silbaram.elasticsearch.dynamic_query_dsl.aggregations

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation
import co.elastic.clients.json.JsonData

open class BucketAggregationDsl {
    var meta: Map<String, JsonData>? = null
    private val subAggregationsDsl = AggregationsDsl()

    fun aggregations(fn: AggregationsDsl.() -> Unit) {
        subAggregationsDsl.fn()
    }

    internal fun applyContainer(container: Aggregation.Builder.ContainerBuilder) {
        meta?.takeIf { it.isNotEmpty() }?.let { container.meta(it) }
        val built = subAggregationsDsl.build()
        if (built.isNotEmpty()) {
            container.aggregations(built)
        }
    }
}

open class FieldBucketAggregationDsl : BucketAggregationDsl() {
    var field: String? = null

    internal fun resolvedField(): String? = field.orNullIfBlank()
}

internal fun String?.orNullIfBlank(): String? = this?.takeIf { it.isNotBlank() }

internal fun Any?.toFieldValueOrNull(): FieldValue? = when (val value = this) {
    null -> null
    is FieldValue -> value
    else -> FieldValue.of(value)
}

internal fun Any?.toJsonDataOrNull(): JsonData? = when (val value = this) {
    null -> null
    is JsonData -> value
    else -> JsonData.of(value)
}

internal fun buildSubAggregations(fn: (AggregationsDsl.() -> Unit)?): Map<String, Aggregation>? {
    if (fn == null) return null
    val nested = AggregationsDsl()
    nested.fn()
    val map = nested.build()
    return map.takeIf { it.isNotEmpty() }
}

internal fun Aggregation.Builder.ContainerBuilder.applyContainer(
    meta: Map<String, JsonData>?,
    aggs: (AggregationsDsl.() -> Unit)?
) {
    meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
    buildSubAggregations(aggs)?.let { aggregations(it) }
}
