package com.github.silbaram.elasticsearch.dynamic_query_dsl.aggregations

import co.elastic.clients.elasticsearch._types.Script
import co.elastic.clients.json.JsonData
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.aggregationOrNull

open class FieldAggregationDsl {
    var field: String? = null
    var script: Script? = null
    var meta: Map<String, JsonData>? = null
}

class FieldMetricAggregationDsl : FieldAggregationDsl() {
    var missing: Any? = null
}

fun AggregationsDsl.avg(name: String, fn: FieldMetricAggregationDsl.() -> Unit) {
    val dsl = FieldMetricAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        avg { builder ->
            dsl.field.orNullIfBlank()?.let { builder.field(it) }
            dsl.script?.let { builder.script(it) }
            dsl.missing.toFieldValueOrNull()?.let { builder.missing(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.sum(name: String, fn: FieldMetricAggregationDsl.() -> Unit) {
    val dsl = FieldMetricAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        sum { builder ->
            dsl.field.orNullIfBlank()?.let { builder.field(it) }
            dsl.script?.let { builder.script(it) }
            dsl.missing.toFieldValueOrNull()?.let { builder.missing(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.min(name: String, fn: FieldMetricAggregationDsl.() -> Unit) {
    val dsl = FieldMetricAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        min { builder ->
            dsl.field.orNullIfBlank()?.let { builder.field(it) }
            dsl.script?.let { builder.script(it) }
            dsl.missing.toFieldValueOrNull()?.let { builder.missing(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.max(name: String, fn: FieldMetricAggregationDsl.() -> Unit) {
    val dsl = FieldMetricAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        max { builder ->
            dsl.field.orNullIfBlank()?.let { builder.field(it) }
            dsl.script?.let { builder.script(it) }
            dsl.missing.toFieldValueOrNull()?.let { builder.missing(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.valueCount(name: String, fn: FieldAggregationDsl.() -> Unit) {
    val dsl = FieldAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        valueCount { builder ->
            dsl.field.orNullIfBlank()?.let { builder.field(it) }
            dsl.script?.let { builder.script(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}
