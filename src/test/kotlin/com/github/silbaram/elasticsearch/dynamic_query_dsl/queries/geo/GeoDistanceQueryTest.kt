package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.geo

import co.elastic.clients.elasticsearch._types.GeoDistanceType
import co.elastic.clients.elasticsearch._types.query_dsl.GeoValidationMethod
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class GeoDistanceQueryTest : FunSpec({

    test("geo_distance 쿼리가 필수 속성과 함께 생성되어야 함") {
        val result = query {
            geoDistanceQuery {
                field = "location"
                distance = "12km"
                distanceType = GeoDistanceType.Arc
                validationMethod = GeoValidationMethod.Strict
                ignoreUnmapped = true
                boost = 1.1f
                _name = "geo-distance"
                location {
                    lat = 37.5
                    lon = 127.0
                }
            }
        }

        result.isGeoDistance shouldBe true
        val geoDistance = result.geoDistance()
        geoDistance.field() shouldBe "location"
        geoDistance.distance() shouldBe "12km"
        geoDistance.distanceType() shouldBe GeoDistanceType.Arc
        geoDistance.validationMethod() shouldBe GeoValidationMethod.Strict
        geoDistance.ignoreUnmapped() shouldBe true
        geoDistance.boost() shouldBe 1.1f
        geoDistance.queryName() shouldBe "geo-distance"
        geoDistance.location().latlon().lat() shouldBe 37.5
        geoDistance.location().latlon().lon() shouldBe 127.0
    }

    test("필수 속성이 없으면 geo_distance 쿼리를 생성하지 않아야 함") {
        queryOrNull {
            geoDistanceQuery {
                field = "location"
                distance = "12km"
            }
        }.shouldBeNull()
    }
})

