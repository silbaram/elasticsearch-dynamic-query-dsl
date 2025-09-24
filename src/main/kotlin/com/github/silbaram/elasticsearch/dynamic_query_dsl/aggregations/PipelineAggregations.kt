package com.github.silbaram.elasticsearch.dynamic_query_dsl.aggregations

import co.elastic.clients.elasticsearch._types.aggregations.AverageBucketAggregation
import co.elastic.clients.elasticsearch._types.aggregations.BucketCorrelationAggregation
import co.elastic.clients.elasticsearch._types.aggregations.BucketKsAggregation
import co.elastic.clients.elasticsearch._types.aggregations.BucketScriptAggregation
import co.elastic.clients.elasticsearch._types.aggregations.BucketSelectorAggregation
import co.elastic.clients.elasticsearch._types.aggregations.BucketSortAggregation
import co.elastic.clients.elasticsearch._types.aggregations.CumulativeCardinalityAggregation
import co.elastic.clients.elasticsearch._types.aggregations.CumulativeSumAggregation
import co.elastic.clients.elasticsearch._types.aggregations.DerivativeAggregation
import co.elastic.clients.elasticsearch._types.aggregations.ExtendedStatsBucketAggregation
import co.elastic.clients.elasticsearch._types.aggregations.InferenceAggregation
import co.elastic.clients.elasticsearch._types.aggregations.MaxBucketAggregation
import co.elastic.clients.elasticsearch._types.aggregations.MinBucketAggregation
import co.elastic.clients.elasticsearch._types.aggregations.MovingFunctionAggregation
import co.elastic.clients.elasticsearch._types.aggregations.MovingPercentilesAggregation
import co.elastic.clients.elasticsearch._types.aggregations.NormalizeAggregation
import co.elastic.clients.elasticsearch._types.aggregations.PercentilesBucketAggregation
import co.elastic.clients.elasticsearch._types.aggregations.SerialDifferencingAggregation
import co.elastic.clients.elasticsearch._types.aggregations.StatsBucketAggregation
import co.elastic.clients.elasticsearch._types.aggregations.SumBucketAggregation
import co.elastic.clients.json.JsonData
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.aggregationOrNull

fun AggregationsDsl.avgBucket(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: AverageBucketAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        avgBucket { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.bucketScript(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: BucketScriptAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        bucketScript { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.bucketCountKsTest(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: BucketKsAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        bucketCountKsTest { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.bucketCorrelation(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: BucketCorrelationAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        bucketCorrelation { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.bucketSelector(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: BucketSelectorAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        bucketSelector { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.bucketSort(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: BucketSortAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        bucketSort { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.cumulativeCardinality(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: CumulativeCardinalityAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        cumulativeCardinality { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.cumulativeSum(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: CumulativeSumAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        cumulativeSum { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.derivative(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: DerivativeAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        derivative { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.extendedStatsBucket(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: ExtendedStatsBucketAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        extendedStatsBucket { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.inference(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: InferenceAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        inference { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.maxBucket(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: MaxBucketAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        maxBucket { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.minBucket(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: MinBucketAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        minBucket { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.movingFn(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: MovingFunctionAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        movingFn { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.movingPercentiles(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: MovingPercentilesAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        movingPercentiles { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.normalize(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: NormalizeAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        normalize { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.percentilesBucket(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: PercentilesBucketAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        percentilesBucket { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.serialDiff(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: SerialDifferencingAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        serialDiff { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.statsBucket(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: StatsBucketAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        statsBucket { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.sumBucket(
    name: String,
    meta: Map<String, JsonData>? = null,
    configure: SumBucketAggregation.Builder.() -> Unit
) {
    val aggregation = aggregationOrNull {
        sumBucket { builder ->
            builder.apply(configure)
            builder
        }.apply {
            applyContainer(meta, null)
        }
    } ?: return
    aggregation(name, aggregation)
}
