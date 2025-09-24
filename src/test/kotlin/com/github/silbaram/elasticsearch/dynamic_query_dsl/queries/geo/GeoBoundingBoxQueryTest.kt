package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.geo

import co.elastic.clients.elasticsearch._types.query_dsl.GeoValidationMethod
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class GeoBoundingBoxQueryTest : FunSpec({

    test("geo_bounding_box 쿼리가 top_left, bottom_right 좌표와 함께 생성되어야 함") {
        val result = query {
            geoBoundingBoxQuery {
                field = "location"
                validationMethod = GeoValidationMethod.Coerce
                ignoreUnmapped = true
                boost = 0.9f
                _name = "geo-box"
                topLeft {
                    lat = 38.0
                    lon = 126.5
                }
                bottomRight {
                    lat = 36.0
                    lon = 128.5
                }
            }
        }

        result.isGeoBoundingBox shouldBe true
        val box = result.geoBoundingBox()
        box.field() shouldBe "location"
        box.validationMethod() shouldBe GeoValidationMethod.Coerce
        box.ignoreUnmapped() shouldBe true
        box.boost() shouldBe 0.9f
        box.queryName() shouldBe "geo-box"
        val bounds = box.boundingBox().tlbr()
        bounds.topLeft().latlon().lat() shouldBe 38.0
        bounds.topLeft().latlon().lon() shouldBe 126.5
        bounds.bottomRight().latlon().lat() shouldBe 36.0
        bounds.bottomRight().latlon().lon() shouldBe 128.5
    }

    test("bounding box가 없으면 geo_bounding_box 쿼리가 생성되지 않아야 함") {
        queryOrNull {
            geoBoundingBoxQuery {
                field = "location"
            }
        }.shouldBeNull()
    }
})

