package com.github.silbaram.elasticsearch.dynamic_query_dsl.aggregations

import co.elastic.clients.elasticsearch._types.Script
import co.elastic.clients.elasticsearch._types.ScriptField
import co.elastic.clients.elasticsearch._types.SortMode
import co.elastic.clients.elasticsearch._types.SortOptions
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.FieldAndFormat
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval
import co.elastic.clients.elasticsearch._types.aggregations.CardinalityExecutionMode
import co.elastic.clients.elasticsearch._types.aggregations.FormatMetricAggregationBase
import co.elastic.clients.elasticsearch._types.aggregations.GeoLinePoint
import co.elastic.clients.elasticsearch._types.aggregations.GeoLineSort
import co.elastic.clients.elasticsearch._types.aggregations.HdrMethod
import co.elastic.clients.elasticsearch._types.aggregations.MetricAggregationBase
import co.elastic.clients.elasticsearch._types.aggregations.RateMode
import co.elastic.clients.elasticsearch._types.aggregations.TDigest
import co.elastic.clients.elasticsearch._types.aggregations.TTestType
import co.elastic.clients.elasticsearch._types.aggregations.TestPopulation
import co.elastic.clients.elasticsearch._types.aggregations.TopMetricsValue
import co.elastic.clients.elasticsearch._types.aggregations.WeightedAverageValue
import co.elastic.clients.elasticsearch._types.aggregations.ValueType
import co.elastic.clients.elasticsearch.core.search.Highlight
import co.elastic.clients.elasticsearch.core.search.SourceConfig
import co.elastic.clients.json.JsonData
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.aggregationOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull

open class FieldAggregationDsl {
    var field: String? = null
    var script: Script? = null
    var meta: Map<String, JsonData>? = null
}

open class FieldMetricAggregationDsl : FieldAggregationDsl() {
    var missing: Any? = null
}

open class FormatMetricAggregationDsl : FieldMetricAggregationDsl() {
    var format: String? = null
}

private fun populateMetricBuilder(dsl: FieldAggregationDsl, builder: MetricAggregationBase.AbstractBuilder<*>) {
    dsl.field.orNullIfBlank()?.let { builder.field(it) }
    dsl.script?.let { builder.script(it) }
}

private fun populateMetricBuilder(dsl: FieldMetricAggregationDsl, builder: MetricAggregationBase.AbstractBuilder<*>) {
    populateMetricBuilder(dsl as FieldAggregationDsl, builder)
    dsl.missing.toFieldValueOrNull()?.let { builder.missing(it) }
}

private fun populateFormatMetricBuilder(
    dsl: FormatMetricAggregationDsl,
    builder: FormatMetricAggregationBase.AbstractBuilder<*>
) {
    populateMetricBuilder(dsl as FieldMetricAggregationDsl, builder)
    dsl.format.orNullIfBlank()?.let { builder.format(it) }
}

class BoxplotAggregationDsl : FieldMetricAggregationDsl() {
    var compression: Double? = null
}

class CardinalityAggregationDsl : FieldMetricAggregationDsl() {
    var precisionThreshold: Int? = null
    var rehash: Boolean? = null
    var executionHint: CardinalityExecutionMode? = null
}

class ExtendedStatsAggregationDsl : FormatMetricAggregationDsl() {
    var sigma: Double? = null
}

class GeoBoundsAggregationDsl : FieldMetricAggregationDsl() {
    var wrapLongitude: Boolean? = null
}

class GeoLineAggregationDsl {
    var meta: Map<String, JsonData>? = null
    var pointField: String? = null
    var sortField: String? = null
    var includeSort: Boolean? = null
    var sortOrder: SortOrder? = null
    var size: Int? = null
}

class MatrixStatsAggregationDsl {
    var meta: Map<String, JsonData>? = null
    private val fields = mutableListOf<String>()
    private val missing = linkedMapOf<String, Double>()
    var mode: SortMode? = null

    fun field(name: String) {
        val resolved = name.orNullIfBlank() ?: return
        fields += resolved
    }

    fun fields(vararg names: String) {
        names.forEach { field(it) }
    }

    fun fields(names: Iterable<String>) {
        names.forEach { field(it) }
    }

    fun missing(field: String, value: Double) {
        val resolved = field.orNullIfBlank() ?: return
        missing[resolved] = value
    }

    fun missing(values: Map<String, Double>) {
        values.forEach { (key, value) -> missing(key, value) }
    }

    internal fun collectFields(): List<String> = fields
    internal fun collectMissing(): Map<String, Double> = missing
}

class MedianAbsoluteDeviationAggregationDsl : FormatMetricAggregationDsl() {
    var compression: Double? = null
}

class PercentilesAggregationDsl : FormatMetricAggregationDsl() {
    var keyed: Boolean? = null
    private val percentValues = mutableListOf<Double>()
    var hdr: HdrMethod? = null
    var tdigest: TDigest? = null

    fun percent(value: Double) {
        percentValues += value
    }

    fun percents(vararg values: Double) {
        percentValues.addAll(values.toList())
    }

    fun percents(values: Iterable<Double>) {
        percentValues.addAll(values)
    }

    fun hdr(fn: HdrMethod.Builder.() -> Unit) {
        hdr = HdrMethod.of { builder ->
            builder.apply(fn)
            builder
        }
    }

    fun tdigest(fn: TDigest.Builder.() -> Unit) {
        tdigest = TDigest.of { builder ->
            builder.apply(fn)
            builder
        }
    }

    internal fun collectPercents(): List<Double> = percentValues
}

class PercentileRanksAggregationDsl : FormatMetricAggregationDsl() {
    var keyed: Boolean? = null
    private val rankValues = mutableListOf<Double>()
    var hdr: HdrMethod? = null
    var tdigest: TDigest? = null

    fun value(rank: Double) {
        rankValues += rank
    }

    fun values(vararg ranks: Double) {
        rankValues.addAll(ranks.toList())
    }

    fun values(ranks: Iterable<Double>) {
        rankValues.addAll(ranks)
    }

    fun hdr(fn: HdrMethod.Builder.() -> Unit) {
        hdr = HdrMethod.of { builder ->
            builder.apply(fn)
            builder
        }
    }

    fun tdigest(fn: TDigest.Builder.() -> Unit) {
        tdigest = TDigest.of { builder ->
            builder.apply(fn)
            builder
        }
    }

    internal fun collectValues(): List<Double> = rankValues
}

class RateAggregationDsl : FormatMetricAggregationDsl() {
    var unit: CalendarInterval? = null
    var mode: RateMode? = null
}

class ScriptedMetricAggregationDsl : FieldMetricAggregationDsl() {
    var combineScript: Script? = null
    var initScript: Script? = null
    var mapScript: Script? = null
    var reduceScript: Script? = null
    private val params = linkedMapOf<String, JsonData>()

    fun param(name: String, value: Any?) {
        val key = name.orNullIfBlank() ?: return
        value.toJsonDataOrNull()?.let { params[key] = it }
    }

    fun params(values: Map<String, Any?>) {
        values.forEach { (key, value) -> param(key, value) }
    }

    internal fun collectParams(): Map<String, JsonData> = params
}

class StringStatsAggregationDsl : FieldMetricAggregationDsl() {
    var showDistribution: Boolean? = null
}

class TestPopulationDsl {
    var field: String? = null
    var script: Script? = null
    var filter: Query? = null

    fun filter(fn: Query.Builder.() -> Unit) {
        filter = queryOrNull(fn)
    }

    internal fun build(): TestPopulation? {
        val resolvedField = field.orNullIfBlank() ?: return null
        return TestPopulation.of { builder ->
            builder.field(resolvedField)
            script?.let { builder.script(it) }
            filter?.let { builder.filter(it) }
            builder
        }
    }
}

class TTestAggregationDsl {
    var meta: Map<String, JsonData>? = null
    private var populationADsl: TestPopulationDsl? = null
    private var populationBDsl: TestPopulationDsl? = null
    var type: TTestType? = null

    fun populationA(fn: TestPopulationDsl.() -> Unit) {
        val dsl = populationADsl ?: TestPopulationDsl()
        dsl.apply(fn)
        populationADsl = dsl
    }

    fun populationB(fn: TestPopulationDsl.() -> Unit) {
        val dsl = populationBDsl ?: TestPopulationDsl()
        dsl.apply(fn)
        populationBDsl = dsl
    }

    internal fun buildPopulationA(): TestPopulation? = populationADsl?.build()
    internal fun buildPopulationB(): TestPopulation? = populationBDsl?.build()
}

class TopHitsAggregationDsl {
    var meta: Map<String, JsonData>? = null
    var explain: Boolean? = null
    var from: Int? = null
    var highlight: Highlight? = null
    var size: Int? = null
    var trackScores: Boolean? = null
    var version: Boolean? = null
    var seqNoPrimaryTerm: Boolean? = null

    private val docvalueFields = mutableListOf<FieldAndFormat>()
    private val fields = mutableListOf<FieldAndFormat>()
    private val scriptFields = linkedMapOf<String, ScriptField>()
    private val sorts = mutableListOf<SortOptions>()
    private val storedFields = mutableListOf<String>()
    var source: SourceConfig? = null

    fun docvalueField(field: FieldAndFormat) {
        docvalueFields += field
    }

    fun docvalueField(field: String, format: String? = null, includeUnmapped: Boolean? = null) {
        val resolved = field.orNullIfBlank() ?: return
        docvalueFields += FieldAndFormat.of { builder ->
            builder.field(resolved)
            format.orNullIfBlank()?.let { builder.format(it) }
            includeUnmapped?.let { builder.includeUnmapped(it) }
            builder
        }
    }

    fun docvalueFields(values: Iterable<FieldAndFormat>) {
        docvalueFields += values
    }

    fun field(field: FieldAndFormat) {
        fields += field
    }

    fun field(name: String, format: String? = null, includeUnmapped: Boolean? = null) {
        val resolved = name.orNullIfBlank() ?: return
        fields += FieldAndFormat.of { builder ->
            builder.field(resolved)
            format.orNullIfBlank()?.let { builder.format(it) }
            includeUnmapped?.let { builder.includeUnmapped(it) }
            builder
        }
    }

    fun fields(values: Iterable<FieldAndFormat>) {
        fields += values
    }

    fun scriptField(name: String, scriptField: ScriptField) {
        val resolved = name.orNullIfBlank() ?: return
        scriptFields[resolved] = scriptField
    }

    fun scriptField(name: String, fn: ScriptField.Builder.() -> Unit) {
        val resolved = name.orNullIfBlank() ?: return
        scriptFields[resolved] = ScriptField.of { builder ->
            builder.apply(fn)
            builder
        }
    }

    fun scriptFields(values: Map<String, ScriptField>) {
        values.forEach { (key, value) -> scriptField(key, value) }
    }

    fun sort(sort: SortOptions) {
        sorts += sort
    }

    fun sort(fn: SortOptions.Builder.() -> Unit) {
        sorts += SortOptions.of { builder ->
            builder.apply(fn)
            builder
        }
    }

    fun sorts(values: Iterable<SortOptions>) {
        sorts += values
    }

    fun storedField(name: String) {
        val resolved = name.orNullIfBlank() ?: return
        storedFields += resolved
    }

    fun storedFields(names: Iterable<String>) {
        names.forEach { storedField(it) }
    }

    fun highlight(fn: Highlight.Builder.() -> Unit) {
        highlight = Highlight.of { builder ->
            builder.apply(fn)
            builder
        }
    }

    fun source(fn: SourceConfig.Builder.() -> Unit) {
        source = SourceConfig.of { builder ->
            builder.apply(fn)
            builder
        }
    }

    internal fun collectDocvalueFields(): List<FieldAndFormat> = docvalueFields
    internal fun collectFields(): List<FieldAndFormat> = fields
    internal fun collectScriptFields(): Map<String, ScriptField> = scriptFields
    internal fun collectSorts(): List<SortOptions> = sorts
    internal fun collectStoredFields(): List<String> = storedFields
}

class TopMetricsAggregationDsl : FieldMetricAggregationDsl() {
    private val metrics = mutableListOf<TopMetricsValue>()
    private val sorts = mutableListOf<SortOptions>()
    var size: Int? = null

    fun metric(value: TopMetricsValue) {
        metrics += value
    }

    fun metric(fn: TopMetricsValue.Builder.() -> Unit) {
        metrics += TopMetricsValue.of { builder ->
            builder.apply(fn)
            builder
        }
    }

    fun metrics(values: Iterable<TopMetricsValue>) {
        metrics += values
    }

    fun sort(sort: SortOptions) {
        sorts += sort
    }

    fun sort(fn: SortOptions.Builder.() -> Unit) {
        sorts += SortOptions.of { builder ->
            builder.apply(fn)
            builder
        }
    }

    fun sorts(values: Iterable<SortOptions>) {
        sorts += values
    }

    internal fun collectMetrics(): List<TopMetricsValue> = metrics
    internal fun collectSorts(): List<SortOptions> = sorts
}

class WeightedAverageAggregationDsl {
    var meta: Map<String, JsonData>? = null
    var format: String? = null
    var valueType: ValueType? = null
    var value: WeightedAverageValue? = null
    var weight: WeightedAverageValue? = null

    fun value(fn: WeightedAverageValue.Builder.() -> Unit) {
        value = WeightedAverageValue.of { builder ->
            builder.apply(fn)
            builder
        }
    }

    fun weight(fn: WeightedAverageValue.Builder.() -> Unit) {
        weight = WeightedAverageValue.of { builder ->
            builder.apply(fn)
            builder
        }
    }
}

fun AggregationsDsl.avg(name: String, fn: FormatMetricAggregationDsl.() -> Unit) {
    val dsl = FormatMetricAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        avg { builder ->
            populateFormatMetricBuilder(dsl, builder)
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.sum(name: String, fn: FormatMetricAggregationDsl.() -> Unit) {
    val dsl = FormatMetricAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        sum { builder ->
            populateFormatMetricBuilder(dsl, builder)
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.min(name: String, fn: FormatMetricAggregationDsl.() -> Unit) {
    val dsl = FormatMetricAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        min { builder ->
            populateFormatMetricBuilder(dsl, builder)
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.max(name: String, fn: FormatMetricAggregationDsl.() -> Unit) {
    val dsl = FormatMetricAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        max { builder ->
            populateFormatMetricBuilder(dsl, builder)
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
            populateMetricBuilder(dsl, builder)
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.boxplot(name: String, fn: BoxplotAggregationDsl.() -> Unit) {
    val dsl = BoxplotAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        boxplot { builder ->
            populateMetricBuilder(dsl, builder)
            dsl.compression?.let { builder.compression(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.cardinality(name: String, fn: CardinalityAggregationDsl.() -> Unit) {
    val dsl = CardinalityAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        cardinality { builder ->
            populateMetricBuilder(dsl, builder)
            dsl.precisionThreshold?.let { builder.precisionThreshold(it) }
            dsl.rehash?.let { builder.rehash(it) }
            dsl.executionHint?.let { builder.executionHint(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.extendedStats(name: String, fn: ExtendedStatsAggregationDsl.() -> Unit) {
    val dsl = ExtendedStatsAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        extendedStats { builder ->
            populateFormatMetricBuilder(dsl, builder)
            dsl.sigma?.let { builder.sigma(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.geoBounds(name: String, fn: GeoBoundsAggregationDsl.() -> Unit) {
    val dsl = GeoBoundsAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        geoBounds { builder ->
            populateMetricBuilder(dsl, builder)
            dsl.wrapLongitude?.let { builder.wrapLongitude(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.geoCentroid(name: String, fn: FieldMetricAggregationDsl.() -> Unit) {
    val dsl = FieldMetricAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        geoCentroid { builder ->
            populateMetricBuilder(dsl, builder)
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.geoLine(name: String, fn: GeoLineAggregationDsl.() -> Unit) {
    val dsl = GeoLineAggregationDsl().apply(fn)
    val pointField = dsl.pointField.orNullIfBlank() ?: return
    val sortField = dsl.sortField.orNullIfBlank() ?: return

    val aggregation = aggregationOrNull {
        geoLine { builder ->
            builder.point(GeoLinePoint.of { point ->
                point.field(pointField)
            })
            builder.sort(GeoLineSort.of { sort ->
                sort.field(sortField)
            })
            dsl.includeSort?.let { builder.includeSort(it) }
            dsl.sortOrder?.let { builder.sortOrder(it) }
            dsl.size?.let { builder.size(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.matrixStats(name: String, fn: MatrixStatsAggregationDsl.() -> Unit) {
    val dsl = MatrixStatsAggregationDsl().apply(fn)
    if (dsl.collectFields().isEmpty()) return

    val aggregation = aggregationOrNull {
        matrixStats { builder ->
            dsl.collectFields().forEach { builder.fields(it) }
            val missing = dsl.collectMissing()
            if (missing.isNotEmpty()) {
                builder.missing(missing)
            }
            dsl.mode?.let { builder.mode(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.medianAbsoluteDeviation(name: String, fn: MedianAbsoluteDeviationAggregationDsl.() -> Unit) {
    val dsl = MedianAbsoluteDeviationAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        medianAbsoluteDeviation { builder ->
            populateFormatMetricBuilder(dsl, builder)
            dsl.compression?.let { builder.compression(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.percentiles(name: String, fn: PercentilesAggregationDsl.() -> Unit) {
    val dsl = PercentilesAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        percentiles { builder ->
            populateFormatMetricBuilder(dsl, builder)
            dsl.keyed?.let { builder.keyed(it) }
            val percents = dsl.collectPercents()
            if (percents.isNotEmpty()) {
                builder.percents(percents)
            }
            dsl.hdr?.let { builder.hdr(it) }
            dsl.tdigest?.let { builder.tdigest(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.percentileRanks(name: String, fn: PercentileRanksAggregationDsl.() -> Unit) {
    val dsl = PercentileRanksAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val values = dsl.collectValues()
    if (values.isEmpty()) return

    val aggregation = aggregationOrNull {
        percentileRanks { builder ->
            populateFormatMetricBuilder(dsl, builder)
            dsl.keyed?.let { builder.keyed(it) }
            builder.values(values)
            dsl.hdr?.let { builder.hdr(it) }
            dsl.tdigest?.let { builder.tdigest(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.rate(name: String, fn: RateAggregationDsl.() -> Unit) {
    val dsl = RateAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        rate { builder ->
            populateFormatMetricBuilder(dsl, builder)
            dsl.unit?.let { builder.unit(it) }
            dsl.mode?.let { builder.mode(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.scriptedMetric(name: String, fn: ScriptedMetricAggregationDsl.() -> Unit) {
    val dsl = ScriptedMetricAggregationDsl().apply(fn)
    if (dsl.mapScript == null) return

    val aggregation = aggregationOrNull {
        scriptedMetric { builder ->
            populateMetricBuilder(dsl, builder)
            dsl.combineScript?.let { builder.combineScript(it) }
            dsl.initScript?.let { builder.initScript(it) }
            dsl.mapScript?.let { builder.mapScript(it) }
            val params = dsl.collectParams()
            if (params.isNotEmpty()) {
                builder.params(params)
            }
            dsl.reduceScript?.let { builder.reduceScript(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.stats(name: String, fn: FormatMetricAggregationDsl.() -> Unit) {
    val dsl = FormatMetricAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        stats { builder ->
            populateFormatMetricBuilder(dsl, builder)
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.stringStats(name: String, fn: StringStatsAggregationDsl.() -> Unit) {
    val dsl = StringStatsAggregationDsl().apply(fn)
    val hasField = dsl.field.orNullIfBlank() != null
    val hasScript = dsl.script != null
    if (!hasField && !hasScript) return

    val aggregation = aggregationOrNull {
        stringStats { builder ->
            populateMetricBuilder(dsl, builder)
            dsl.showDistribution?.let { builder.showDistribution(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.tTest(name: String, fn: TTestAggregationDsl.() -> Unit) {
    val dsl = TTestAggregationDsl().apply(fn)
    val populationA = dsl.buildPopulationA() ?: return
    val populationB = dsl.buildPopulationB() ?: return

    val aggregation = aggregationOrNull {
        tTest { builder ->
            builder.a(populationA)
            builder.b(populationB)
            dsl.type?.let { builder.type(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.topHits(name: String, fn: TopHitsAggregationDsl.() -> Unit) {
    val dsl = TopHitsAggregationDsl().apply(fn)

    val aggregation = aggregationOrNull {
        topHits { builder ->
            val docvalueFields = dsl.collectDocvalueFields()
            if (docvalueFields.isNotEmpty()) {
                builder.docvalueFields(docvalueFields)
            }
            dsl.explain?.let { builder.explain(it) }
            val fields = dsl.collectFields()
            if (fields.isNotEmpty()) {
                builder.fields(fields)
            }
            dsl.from?.let { builder.from(it) }
            dsl.highlight?.let { builder.highlight(it) }
            val scriptFields = dsl.collectScriptFields()
            if (scriptFields.isNotEmpty()) {
                builder.scriptFields(scriptFields)
            }
            dsl.size?.let { builder.size(it) }
            val sorts = dsl.collectSorts()
            if (sorts.isNotEmpty()) {
                builder.sort(sorts)
            }
            dsl.source?.let { builder.source(it) }
            val storedFields = dsl.collectStoredFields()
            if (storedFields.isNotEmpty()) {
                builder.storedFields(storedFields)
            }
            dsl.trackScores?.let { builder.trackScores(it) }
            dsl.version?.let { builder.version(it) }
            dsl.seqNoPrimaryTerm?.let { builder.seqNoPrimaryTerm(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return

    aggregation(name, aggregation)
}

fun AggregationsDsl.topMetrics(name: String, fn: TopMetricsAggregationDsl.() -> Unit) {
    val dsl = TopMetricsAggregationDsl().apply(fn)
    val metrics = dsl.collectMetrics()
    val sorts = dsl.collectSorts()
    if (metrics.isEmpty() || sorts.isEmpty()) return

    val aggregation = aggregationOrNull {
        topMetrics { builder ->
            populateMetricBuilder(dsl, builder)
            builder.metrics(metrics)
            dsl.size?.let { builder.size(it) }
            builder.sort(sorts)
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}

fun AggregationsDsl.weightedAvg(name: String, fn: WeightedAverageAggregationDsl.() -> Unit) {
    val dsl = WeightedAverageAggregationDsl().apply(fn)
    val value = dsl.value ?: return

    val aggregation = aggregationOrNull {
        weightedAvg { builder ->
            dsl.format.orNullIfBlank()?.let { builder.format(it) }
            builder.value(value)
            dsl.weight?.let { builder.weight(it) }
            dsl.valueType?.let { builder.valueType(it) }
            builder
        }.apply {
            dsl.meta?.takeIf { it.isNotEmpty() }?.let { meta(it) }
        }
    } ?: return
    aggregation(name, aggregation)
}
