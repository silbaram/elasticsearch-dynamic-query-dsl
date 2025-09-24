package com.github.silbaram.elasticsearch.dynamic_query_dsl.aggregations

import co.elastic.clients.elasticsearch._types.aggregations.AggregationRange
import co.elastic.clients.elasticsearch._types.aggregations.Buckets
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregationSource
import co.elastic.clients.elasticsearch._types.aggregations.DateRangeExpression
import co.elastic.clients.elasticsearch._types.aggregations.FrequentItemSetsField
import co.elastic.clients.elasticsearch._types.aggregations.IpRangeAggregationRange
import co.elastic.clients.elasticsearch._types.aggregations.MultiTermLookup
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.shouldBe
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query

class BucketAggregationsTest : FunSpec({
    test("builds bucket aggregation variants with builder DSL") {
        val aggregations = aggregations {
            adjacencyMatrix("adj") {
                filters("all") { query ->
                    query.matchAll { it }
                    query
                }
            }
            autoDateHistogram("auto_date") {
                field("timestamp")
                buckets(12)
            }
            categorizeText("categorize") {
                field("message")
            }
            children("children") {
                type("comment")
            }
            composite("composite") {
                sources(
                    listOf(
                        mapOf(
                            "category" to CompositeAggregationSource.of { source ->
                                source.terms { term -> term.field("category") }
                            }
                        )
                    )
                )
            }
            dateHistogram("date_hist") {
                field("timestamp")
                calendarInterval(CalendarInterval.Day)
            }
            dateRange("date_range") {
                field("timestamp")
                ranges(
                    DateRangeExpression.of { expr ->
                        expr.from { fm -> fm.expr("now-1d") }
                    }
                )
            }
            diversifiedSampler("diversified") {
                field("user_id")
                shardSize(100)
            }
            filter("filter") {
                matchAll { it }
            }
            filters("filters") {
                filters(
                    Buckets.of { bucket ->
                        bucket.keyed(
                            mapOf(
                                "all" to query { matchAll { it } }
                            )
                        )
                    }
                )
            }
            frequentItemSets("frequent") {
                fields(FrequentItemSetsField.of { it.field("tags") })
                minimumSetSize(2)
            }
            geoDistance("geo_dist") {
                field("location")
                origin { it.text("0,0") }
                ranges(AggregationRange.of { range -> range.to(1000.0) })
            }
            geoHashGrid("geo_hash") {
                field("location")
                precision { p -> p.geohashLength(3) }
            }
            geoHexGrid("geo_hex") {
                field("location")
                precision(3)
            }
            geoTileGrid("geo_tile") {
                field("location")
                precision(3)
            }
            global("global")
            histogram("histogram") {
                field("price")
                interval(5.0)
            }
            ipPrefix("ip_prefix") {
                field("ip")
                prefixLength(24)
            }
            ipRange("ip_range") {
                field("ip")
                ranges(
                    IpRangeAggregationRange.of { range -> range.to("10.0.0.127") }
                )
            }
            missing("missing", "missing_field")
            multiTerms("multi_terms") {
                terms(
                    MultiTermLookup.of { it.field("brand") }
                )
            }
            nested("nested", "nested_field")
            parent("parent", "parent_type")
            randomSampler("random_sampler", probability = 0.5)
            range("range") {
                field("price")
                ranges(AggregationRange.of { range -> range.to(10.0) })
            }
            rareTerms("rare_terms") {
                field("keyword")
            }
            reverseNested("reverse_nested")
            sampler("sampler") {
                shardSize(100)
            }
            significantTerms("sig_terms") {
                field("keyword")
            }
            significantText("sig_text", "description")
            terms("terms") {
                field = "keyword"
                size = 5
            }
            timeSeries("time_series", size = 10, keyed = true)
            variableWidthHistogram("variable_hist") {
                field("amount")
                buckets(4)
            }
        }

        aggregations.keys.shouldHaveSize(33)
        aggregations["adj"]?.adjacencyMatrix() shouldNotBe null
        aggregations["auto_date"]?.autoDateHistogram() shouldNotBe null
        aggregations["categorize"]?.categorizeText() shouldNotBe null
        aggregations["children"]?.children() shouldNotBe null
        aggregations["composite"]?.composite() shouldNotBe null
        aggregations["date_hist"]?.dateHistogram() shouldNotBe null
        aggregations["date_range"]?.dateRange() shouldNotBe null
        aggregations["diversified"]?.diversifiedSampler() shouldNotBe null
        aggregations["filter"]?.filter() shouldNotBe null
        aggregations["filters"]?.filters() shouldNotBe null
        aggregations["frequent"]?.frequentItemSets() shouldNotBe null
        aggregations["geo_dist"]?.geoDistance() shouldNotBe null
        aggregations["geo_hash"]?.geohashGrid() shouldNotBe null
        aggregations["geo_hex"]?.geohexGrid() shouldNotBe null
        aggregations["geo_tile"]?.geotileGrid() shouldNotBe null
        aggregations["global"]?.global() shouldNotBe null
        aggregations["histogram"]?.histogram() shouldNotBe null
        aggregations["ip_prefix"]?.ipPrefix() shouldNotBe null
        aggregations["ip_range"]?.ipRange() shouldNotBe null
        aggregations["missing"]?.missing() shouldNotBe null
        aggregations["multi_terms"]?.multiTerms() shouldNotBe null
        aggregations["nested"]?.nested() shouldNotBe null
        aggregations["parent"]?.parent() shouldNotBe null
        aggregations["random_sampler"]?._customKind() shouldBe "random_sampler"
        aggregations["range"]?.range() shouldNotBe null
        aggregations["rare_terms"]?.rareTerms() shouldNotBe null
        aggregations["reverse_nested"]?.reverseNested() shouldNotBe null
        aggregations["sampler"]?.sampler() shouldNotBe null
        aggregations["sig_terms"]?.significantTerms() shouldNotBe null
        aggregations["sig_text"]?.significantText() shouldNotBe null
        aggregations["terms"]?.terms() shouldNotBe null
        aggregations["time_series"]?._customKind() shouldBe "time_series"
        aggregations["variable_hist"]?.variableWidthHistogram() shouldNotBe null
    }
})
