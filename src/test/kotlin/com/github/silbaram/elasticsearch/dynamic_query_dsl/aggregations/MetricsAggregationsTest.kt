package com.github.silbaram.elasticsearch.dynamic_query_dsl.aggregations

import co.elastic.clients.elasticsearch._types.Script
import co.elastic.clients.elasticsearch._types.ScriptField
import co.elastic.clients.elasticsearch._types.SortMode
import co.elastic.clients.elasticsearch._types.SortOptions
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval
import co.elastic.clients.elasticsearch._types.aggregations.CardinalityExecutionMode
import co.elastic.clients.elasticsearch._types.aggregations.RateMode
import co.elastic.clients.elasticsearch._types.aggregations.TTestType
import co.elastic.clients.elasticsearch._types.aggregations.ValueType
import co.elastic.clients.elasticsearch.core.search.HighlightField
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldNotBe
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query

class MetricsAggregationsTest : FunSpec({
    fun inlineScript(source: String): Script = Script.of { script ->
        script.inline { inline -> inline.source(source) }
    }

    test("builds metric aggregation variants with builder DSL") {
        val aggregations = aggregations {
            avg("avg_price") {
                field = "price"
                format = "0.00"
            }
            boxplot("box_price") {
                field = "price"
                compression = 120.0
            }
            cardinality("unique_sku") {
                field = "sku"
                precisionThreshold = 1000
                rehash = true
                executionHint = CardinalityExecutionMode.GlobalOrdinals
            }
            extendedStats("extended_price") {
                field = "price"
                sigma = 2.0
            }
            geoBounds("geo_bounds") {
                field = "location"
                wrapLongitude = true
            }
            geoCentroid("geo_centroid") {
                field = "location"
            }
            geoLine("geo_line") {
                pointField = "trajectory"
                sortField = "timestamp"
                sortOrder = SortOrder.Desc
                size = 50
            }
            matrixStats("matrix_stats") {
                field("price")
                field("quantity")
                missing("quantity", 0.0)
                mode = SortMode.Avg
            }
            medianAbsoluteDeviation("mad_price") {
                field = "price"
                compression = 200.0
            }
            min("min_price") { field = "price" }
            max("max_price") { field = "price" }
            percentiles("percentiles_price") {
                field = "price"
                keyed = true
                percents(25.0, 50.0, 75.0)
                hdr { numberOfSignificantValueDigits(3) }
            }
            percentileRanks("percentile_ranks_price") {
                field = "price"
                values(42.0, 100.0)
                tdigest { compression(150) }
            }
            rate("orders_rate") {
                field = "orders"
                unit = CalendarInterval.Hour
                mode = RateMode.Sum
            }
            scriptedMetric("scripted_metric") {
                initScript = inlineScript("state = []")
                mapScript = inlineScript("state.add(doc['price'].value)")
                combineScript = inlineScript("state")
                reduceScript = inlineScript("double sum = 0; for (state in states) { sum += state.stream().mapToDouble(Double::doubleValue).sum(); } return sum;")
                param("factor", 2)
            }
            stats("stats_price") { field = "price" }
            stringStats("string_stats") {
                field = "keyword"
                showDistribution = true
            }
            sum("sum_price") { field = "price" }
            tTest("ttest_groups") {
                populationA {
                    field = "metric"
                    filter {
                        term { term -> term.field("group").value("A") }
                    }
                }
                populationB {
                    field = "metric"
                    filter {
                        term { term -> term.field("group").value("B") }
                    }
                }
                type = TTestType.Heteroscedastic
            }
            topHits("top_hits") {
                size = 3
                docvalueField("timestamp", "epoch_millis")
                field("description")
                storedField("description")
                sort {
                    field { fieldSort -> fieldSort.field("timestamp").order(SortOrder.Desc) }
                }
                highlight {
                    fields("description", HighlightField.of { field -> field })
                }
                scriptField("discount") {
                    script(inlineScript("doc['price'].value * 0.9"))
                }
                source { fetch(true) }
            }
            topMetrics("top_metrics") {
                field = "price"
                metric {
                    field("price")
                }
                sort {
                    field { fieldSort -> fieldSort.field("timestamp").order(SortOrder.Desc) }
                }
                size = 1
            }
            valueCount("value_count") { field = "price" }
            weightedAvg("weighted_avg") {
                format = "0.0"
                valueType = ValueType.Double
                value { field("price") }
                weight { field("weight") }
            }
        }

        aggregations.keys.shouldHaveSize(23)
        aggregations["avg_price"]?.avg() shouldNotBe null
        aggregations["box_price"]?.boxplot() shouldNotBe null
        aggregations["unique_sku"]?.cardinality() shouldNotBe null
        aggregations["extended_price"]?.extendedStats() shouldNotBe null
        aggregations["geo_bounds"]?.geoBounds() shouldNotBe null
        aggregations["geo_centroid"]?.geoCentroid() shouldNotBe null
        aggregations["geo_line"]?.geoLine() shouldNotBe null
        aggregations["matrix_stats"]?.matrixStats() shouldNotBe null
        aggregations["mad_price"]?.medianAbsoluteDeviation() shouldNotBe null
        aggregations["min_price"]?.min() shouldNotBe null
        aggregations["max_price"]?.max() shouldNotBe null
        aggregations["percentiles_price"]?.percentiles() shouldNotBe null
        aggregations["percentile_ranks_price"]?.percentileRanks() shouldNotBe null
        aggregations["orders_rate"]?.rate() shouldNotBe null
        aggregations["scripted_metric"]?.scriptedMetric() shouldNotBe null
        aggregations["stats_price"]?.stats() shouldNotBe null
        aggregations["string_stats"]?.stringStats() shouldNotBe null
        aggregations["sum_price"]?.sum() shouldNotBe null
        aggregations["ttest_groups"]?.tTest() shouldNotBe null
        aggregations["top_hits"]?.topHits() shouldNotBe null
        aggregations["top_metrics"]?.topMetrics() shouldNotBe null
        aggregations["value_count"]?.valueCount() shouldNotBe null
        aggregations["weighted_avg"]?.weightedAvg() shouldNotBe null
    }
})
