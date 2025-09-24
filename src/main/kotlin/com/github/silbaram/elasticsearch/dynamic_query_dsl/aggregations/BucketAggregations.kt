package com.github.silbaram.elasticsearch.dynamic_query_dsl.aggregations

import co.elastic.clients.elasticsearch._types.aggregations.AdjacencyMatrixAggregation
import co.elastic.clients.elasticsearch._types.aggregations.AutoDateHistogramAggregation
import co.elastic.clients.elasticsearch._types.aggregations.CategorizeTextAggregation
import co.elastic.clients.elasticsearch._types.aggregations.ChildrenAggregation
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregation
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregationSource
import co.elastic.clients.elasticsearch._types.aggregations.DateHistogramAggregation
import co.elastic.clients.elasticsearch._types.aggregations.DateRangeAggregation
import co.elastic.clients.elasticsearch._types.aggregations.DateRangeExpression
import co.elastic.clients.elasticsearch._types.aggregations.DiversifiedSamplerAggregation
import co.elastic.clients.elasticsearch._types.aggregations.FiltersAggregation
import co.elastic.clients.elasticsearch._types.aggregations.FrequentItemSetsAggregation
import co.elastic.clients.elasticsearch._types.aggregations.FrequentItemSetsField
import co.elastic.clients.elasticsearch._types.aggregations.GeoDistanceAggregation
import co.elastic.clients.elasticsearch._types.aggregations.GeoHashGridAggregation
import co.elastic.clients.elasticsearch._types.aggregations.GeoTileGridAggregation
import co.elastic.clients.elasticsearch._types.aggregations.GeohexGridAggregation
import co.elastic.clients.elasticsearch._types.aggregations.GlobalAggregation
import co.elastic.clients.elasticsearch._types.aggregations.HistogramAggregation
import co.elastic.clients.elasticsearch._types.aggregations.IpPrefixAggregation
import co.elastic.clients.elasticsearch._types.aggregations.IpRangeAggregation
import co.elastic.clients.elasticsearch._types.aggregations.IpRangeAggregationRange
import co.elastic.clients.elasticsearch._types.aggregations.MissingAggregation
import co.elastic.clients.elasticsearch._types.aggregations.MultiTermLookup
import co.elastic.clients.elasticsearch._types.aggregations.MultiTermsAggregation
import co.elastic.clients.elasticsearch._types.aggregations.NestedAggregation
import co.elastic.clients.elasticsearch._types.aggregations.ParentAggregation
import co.elastic.clients.elasticsearch._types.aggregations.RangeAggregation
import co.elastic.clients.elasticsearch._types.aggregations.AggregationRange
import co.elastic.clients.elasticsearch._types.aggregations.RareTermsAggregation
import co.elastic.clients.elasticsearch._types.aggregations.ReverseNestedAggregation
import co.elastic.clients.elasticsearch._types.aggregations.SamplerAggregation
import co.elastic.clients.elasticsearch._types.aggregations.SignificantTermsAggregation
import co.elastic.clients.elasticsearch._types.aggregations.SignificantTextAggregation
import co.elastic.clients.elasticsearch._types.aggregations.VariableWidthHistogramAggregation
import co.elastic.clients.elasticsearch._types.aggregations.Buckets
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.json.JsonData
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.aggregationOrNull

fun AggregationsDsl.adjacencyMatrix(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: AdjacencyMatrixAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        adjacencyMatrix { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.autoDateHistogram(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: AutoDateHistogramAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        autoDateHistogram { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.categorizeText(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: CategorizeTextAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        categorizeText { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.children(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: ChildrenAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        children { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.composite(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: CompositeAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        composite { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.dateHistogram(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: DateHistogramAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        dateHistogram { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.dateRange(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: DateRangeAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        dateRange { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.diversifiedSampler(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: DiversifiedSamplerAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        diversifiedSampler { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.filter(
    name: String,
    filter: Query,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null
) {
    val aggregation = aggregationOrNull {
        filter(filter).apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.filter(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: Query.Builder.() -> Unit
) {
    val query = queryOrNull(configure) ?: return
    filter(name, query, meta, aggs)
}

fun AggregationsDsl.filters(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: FiltersAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        filters { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.frequentItemSets(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: FrequentItemSetsAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        frequentItemSets { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.geoDistance(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: GeoDistanceAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        geoDistance { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.geoHashGrid(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: GeoHashGridAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        geohashGrid { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.geoHexGrid(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: GeohexGridAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        geohexGrid { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.geoTileGrid(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: GeoTileGridAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        geotileGrid { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.global(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null
) {
    val aggregation = aggregationOrNull {
        global { builder -> builder }
            .apply { applyContainer(meta, aggs) }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.histogram(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: HistogramAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        histogram { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.ipPrefix(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: IpPrefixAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        ipPrefix { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.ipRange(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: IpRangeAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        ipRange { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.missing(
    name: String,
    field: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null
) {
    val aggregation = aggregationOrNull {
        missing { builder ->
            builder.field(field)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.multiTerms(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: MultiTermsAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        multiTerms { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.nested(
    name: String,
    path: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null
) {
    val aggregation = aggregationOrNull {
        nested { builder ->
            builder.path(path)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.parent(
    name: String,
    parentType: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null
) {
    val aggregation = aggregationOrNull {
        parent { builder ->
            builder.type(parentType)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.randomSampler(
    name: String,
    probability: Double,
    seed: Long? = null,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null
) {
    val payload = buildMap<String, Any?> {
        put("probability", probability)
        seed?.let { put("seed", it) }
    }
    val aggregation = aggregationOrNull {
        _custom("random_sampler", payload).apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.range(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: RangeAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        range { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.rareTerms(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: RareTermsAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        rareTerms { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.reverseNested(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: ReverseNestedAggregation.Builder.() -> Unit = {}
) {
    val aggregation = aggregationOrNull {
        reverseNested { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.sampler(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: SamplerAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        sampler { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.significantTerms(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: SignificantTermsAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        significantTerms { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.significantText(
    name: String,
    field: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: SignificantTextAggregation.Builder.() -> Unit = {}
) {
    val aggregation = aggregationOrNull {
        significantText { builder ->
            builder.field(field)
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.timeSeries(
    name: String,
    size: Int? = null,
    keyed: Boolean? = null,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null
) {
    val payload = buildMap<String, Any?> {
        size?.let { put("size", it) }
        keyed?.let { put("keyed", it) }
    }
    val aggregation = aggregationOrNull {
        _custom("time_series", payload).apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.variableWidthHistogram(
    name: String,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null,
    configure: VariableWidthHistogramAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        variableWidthHistogram { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, aggs)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.filters(
    name: String,
    keyedFilters: Map<String, Query>,
    meta: Map<String, JsonData>? = null,
    aggs: (AggregationsDsl.() -> Unit)? = null
) {
    filters(name, meta, aggs) {
        filters(
            Buckets.of { b ->
                b.keyed(keyedFilters)
            }
        )
    }
}
