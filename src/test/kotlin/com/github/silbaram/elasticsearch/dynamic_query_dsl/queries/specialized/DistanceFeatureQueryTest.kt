package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class DistanceFeatureQueryTest : FunSpec({

    test("distance_feature: date origin - DSL style") {
        val q = query {
            distanceFeatureQuery {
                field = "production_date"
                origin = "now"
                pivot = "7d"
                boost = 1.2f
                _name = "date-recency-boost"
            }
        }

        q.shouldNotBeNull()
        q.isDistanceFeature shouldBe true
        val df = q.distanceFeature()
        df.field() shouldBe "production_date"
        df.boost() shouldBe 1.2f
        df.queryName() shouldBe "date-recency-boost"
        q.toString().contains("\"pivot\":\"7d\"") shouldBe true
        q.toString().contains("\"origin\":\"now\"") shouldBe true
    }

    test("distance_feature: geo origin(lat/lon) - DSL style") {
        val q = query {
            distanceFeatureQuery {
                field = "location"
                origin(52.376, 4.894)
                pivot = "2km"
                _name = "geo-proximity"
            }
        }

        q.isDistanceFeature shouldBe true
        val df = q.distanceFeature()
        df.field() shouldBe "location"
        df.queryName() shouldBe "geo-proximity"
        q.toString().contains("\"pivot\":\"2km\"") shouldBe true
    }

    test("distance_feature: invalid inputs are omitted (null/blank)") {
        val built = query {
            distanceFeatureQuery {
                field = "" // invalid
                origin = "now"
                pivot = "7d"
            }
            // no-op fallback 유지
            matchAll { it }
        }
        built.isMatchAll shouldBe true

        val built2 = query {
            distanceFeatureQuery {
                field = "date"
                origin = null
                pivot = "7d"
            }
            matchAll { it }
        }
        built2.isMatchAll shouldBe true

        val built3 = query {
            distanceFeatureQuery {
                field = "date"
                origin = "now"
                pivot = " " // invalid
            }
            matchAll { it }
        }
        built3.isMatchAll shouldBe true
    }
})
