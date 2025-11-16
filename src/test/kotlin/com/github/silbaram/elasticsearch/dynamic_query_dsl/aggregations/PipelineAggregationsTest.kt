package com.github.silbaram.elasticsearch.dynamic_query_dsl.aggregations

import co.elastic.clients.elasticsearch._types.Script
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.aggregations.BucketCorrelationFunction
import co.elastic.clients.elasticsearch._types.aggregations.BucketCorrelationFunctionCountCorrelation
import co.elastic.clients.elasticsearch._types.aggregations.BucketCorrelationFunctionCountCorrelationIndicator
import co.elastic.clients.elasticsearch._types.aggregations.BucketsPath
import co.elastic.clients.elasticsearch._types.aggregations.GapPolicy
import co.elastic.clients.elasticsearch._types.aggregations.NormalizeMethod
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldNotBe

class PipelineAggregationsTest : FunSpec({
    fun inlineScript(source: String): Script = Script.of { script ->
        script.source(source)
    }

    fun singlePath(path: String): BucketsPath = BucketsPath.of { builder ->
        builder.single(path)
    }

    test("builds pipeline aggregation variants with builder DSL") {
        val aggregations = aggregations {
            avgBucket("avg_bucket") {
                bucketsPath(singlePath("sales>sum"))
            }
            bucketScript("bucket_script") {
                bucketsPath(BucketsPath.of { builder ->
                    builder.dict(mapOf("total" to "sales>sum"))
                })
                script(inlineScript("params.total * params.factor"))
                format("0.00")
            }
            bucketCountKsTest("bucket_ks") {
                bucketsPath(BucketsPath.of { builder ->
                    builder.array(listOf("sales>percentiles[50.0]", "sales>percentiles[95.0]"))
                })
                alternative("greater", "less")
                fractions(0.5, 0.5)
                samplingMethod("upper_tail")
            }
            bucketCorrelation("bucket_corr") {
                bucketsPath(singlePath("sales>sum"))
                function(
                    BucketCorrelationFunction.of { fn ->
                        fn.countCorrelation(
                            BucketCorrelationFunctionCountCorrelation.of { cc ->
                                cc.indicator(
                                    BucketCorrelationFunctionCountCorrelationIndicator.of { indicator ->
                                        indicator.docCount(100)
                                        indicator.expectations(listOf(0.2, 0.3, 0.5))
                                        indicator.fractions(listOf(0.2, 0.3, 0.5))
                                        indicator
                                    }
                                )
                                cc
                            }
                        )
                        fn
                    }
                )
            }
            bucketSelector("bucket_selector") {
                bucketsPath(BucketsPath.of { builder ->
                    builder.dict(mapOf("total" to "sales>sum"))
                })
                script(inlineScript("params.total > 100"))
                gapPolicy(GapPolicy.Skip)
            }
            bucketSort("bucket_sort") {
                sort { sortBuilder ->
                    sortBuilder.field { fieldSort -> fieldSort.field("_key").order(SortOrder.Asc) }
                }
                size(5)
            }
            cumulativeCardinality("cumulative_cardinality") {
                bucketsPath(singlePath("sales>unique"))
            }
            cumulativeSum("cumulative_sum") {
                bucketsPath(singlePath("sales>sum"))
                format("0.00")
            }
            derivative("derivative") {
                bucketsPath(singlePath("sales>sum"))
            }
            extendedStatsBucket("extended_stats_bucket") {
                bucketsPath(singlePath("sales>sum"))
                sigma(2.0)
            }
            inference("inference") {
                bucketsPath(singlePath("sales>sum"))
                modelId("test-model")
            }
            maxBucket("max_bucket") {
                bucketsPath(singlePath("sales>sum"))
            }
            minBucket("min_bucket") {
                bucketsPath(singlePath("sales>sum"))
            }
            movingFn("moving_fn") {
                bucketsPath(singlePath("sales>sum"))
                script("MovingFunctions.unweightedAvg(values)")
                window(3)
            }
            movingPercentiles("moving_percentiles") {
                bucketsPath(singlePath("sales>percentiles[50.0]"))
                window(3)
                keyed(true)
            }
            normalize("normalize") {
                bucketsPath(singlePath("sales>sum"))
                method(NormalizeMethod.Rescale_0_1)
            }
            percentilesBucket("percentiles_bucket") {
                bucketsPath(singlePath("sales>sum"))
                percents(50.0, 90.0)
            }
            serialDiff("serial_diff") {
                bucketsPath(singlePath("sales>sum"))
                lag(2)
            }
            statsBucket("stats_bucket") {
                bucketsPath(singlePath("sales>sum"))
            }
            sumBucket("sum_bucket") {
                bucketsPath(singlePath("sales>sum"))
            }
        }

        aggregations.keys.shouldHaveSize(20)
        aggregations["avg_bucket"]?.avgBucket() shouldNotBe null
        aggregations["bucket_script"]?.bucketScript() shouldNotBe null
        aggregations["bucket_ks"]?.bucketCountKsTest() shouldNotBe null
        aggregations["bucket_corr"]?.bucketCorrelation() shouldNotBe null
        aggregations["bucket_selector"]?.bucketSelector() shouldNotBe null
        aggregations["bucket_sort"]?.bucketSort() shouldNotBe null
        aggregations["cumulative_cardinality"]?.cumulativeCardinality() shouldNotBe null
        aggregations["cumulative_sum"]?.cumulativeSum() shouldNotBe null
        aggregations["derivative"]?.derivative() shouldNotBe null
        aggregations["extended_stats_bucket"]?.extendedStatsBucket() shouldNotBe null
        aggregations["inference"]?.inference() shouldNotBe null
        aggregations["max_bucket"]?.maxBucket() shouldNotBe null
        aggregations["min_bucket"]?.minBucket() shouldNotBe null
        aggregations["moving_fn"]?.movingFn() shouldNotBe null
        aggregations["moving_percentiles"]?.movingPercentiles() shouldNotBe null
        aggregations["normalize"]?.normalize() shouldNotBe null
        aggregations["percentiles_bucket"]?.percentilesBucket() shouldNotBe null
        aggregations["serial_diff"]?.serialDiff() shouldNotBe null
        aggregations["stats_bucket"]?.statsBucket() shouldNotBe null
        aggregations["sum_bucket"]?.sumBucket() shouldNotBe null
    }
})
