package com.github.silbaram.elasticsearch.dynamic_query_dsl.aggregations

import co.elastic.clients.elasticsearch._types.Script
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.aggregations.MissingOrder
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregationCollectMode
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregationExecutionHint
import co.elastic.clients.elasticsearch._types.aggregations.TermsExclude
import co.elastic.clients.elasticsearch._types.aggregations.TermsInclude
import co.elastic.clients.util.NamedValue
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.aggregationOrNull

class TermsAggregationDsl : FieldBucketAggregationDsl() {
    var script: Script? = null
    var collectMode: TermsAggregationCollectMode? = null
    var executionHint: TermsAggregationExecutionHint? = null
    var include: TermsInclude? = null
    var exclude: TermsExclude? = null
    var size: Int? = null
    var minDocCount: Int? = null
    var shardSize: Int? = null
    var shardMinDocCount: Long? = null
    var showTermDocCountError: Boolean? = null
    var missing: Any? = null
    var missingBucket: Boolean? = null
    var missingOrder: MissingOrder? = null
    var valueType: String? = null
    var format: String? = null

    private val orders = mutableListOf<NamedValue<SortOrder>>()

    fun orderBy(key: String, sortOrder: SortOrder) {
        val resolved = key.orNullIfBlank() ?: return
        orders += NamedValue.of(resolved, sortOrder)
    }

    fun orderByCount(sortOrder: SortOrder = SortOrder.Desc) {
        orders += NamedValue.of("_count", sortOrder)
    }

    fun orderByKey(sortOrder: SortOrder = SortOrder.Asc) {
        orders += NamedValue.of("_key", sortOrder)
    }

    internal fun apply(builder: co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation.Builder) {
        resolvedField()?.let { builder.field(it) }
        script?.let { builder.script(it) }
        collectMode?.let { builder.collectMode(it) }
        executionHint?.let { builder.executionHint(it) }
        include?.let { builder.include(it) }
        exclude?.let { builder.exclude(it) }
        size?.let { builder.size(it) }
        minDocCount?.let { builder.minDocCount(it) }
        shardSize?.let { builder.shardSize(it) }
        shardMinDocCount?.let { builder.shardMinDocCount(it) }
        showTermDocCountError?.let { builder.showTermDocCountError(it) }
        missing.toFieldValueOrNull()?.let { builder.missing(it) }
        missingBucket?.let { builder.missingBucket(it) }
        missingOrder?.let { builder.missingOrder(it) }
        valueType.orNullIfBlank()?.let { builder.valueType(it) }
        format.orNullIfBlank()?.let { builder.format(it) }
        if (orders.isNotEmpty()) {
            builder.order(orders)
        }
    }
}

fun AggregationsDsl.terms(name: String, fn: TermsAggregationDsl.() -> Unit) {
    val dsl = TermsAggregationDsl().apply(fn)
    val hasField = dsl.resolvedField() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        terms { builder ->
            dsl.apply(builder)
            builder
        }.apply {
            dsl.applyContainer(this)
        }
    } ?: return
    aggregation(name, aggregation)
}
