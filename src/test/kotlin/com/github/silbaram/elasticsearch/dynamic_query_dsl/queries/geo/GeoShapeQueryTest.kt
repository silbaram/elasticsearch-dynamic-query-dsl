package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.geo

import co.elastic.clients.elasticsearch._types.GeoShapeRelation
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class GeoShapeQueryTest : FunSpec({

    test("geo_shape 쿼리가 inline shape과 relation을 반영해야 함") {
        val polygon = mapOf(
            "type" to "polygon",
            "coordinates" to listOf(
                listOf(
                    listOf(125.0, 35.0),
                    listOf(128.0, 35.0),
                    listOf(128.0, 33.0),
                    listOf(125.0, 33.0),
                    listOf(125.0, 35.0)
                )
            )
        )

        val result = query {
            geoShapeQuery {
                field = "geometry"
                relation = GeoShapeRelation.Within
                ignoreUnmapped = false
                boost = 1.2f
                _name = "geo-shape"
                shape(polygon)
            }
        }

        result.isGeoShape shouldBe true
        val geoShape = result.geoShape()
        geoShape.field() shouldBe "geometry"
        geoShape.ignoreUnmapped() shouldBe false
        geoShape.boost() shouldBe 1.2f
        geoShape.queryName() shouldBe "geo-shape"
        geoShape.shape().relation() shouldBe GeoShapeRelation.Within
        geoShape.shape().shape()?.toString()!!.contains("polygon") shouldBe true
    }

    test("indexed_shape를 사용한 geo_shape 쿼리가 생성되어야 함") {
        val result = query {
            geoShapeQuery {
                field = "geometry"
                indexedShape {
                    id = "shape-1"
                    index = "shapes"
                    path = "geometry"
                }
            }
        }

        result.isGeoShape shouldBe true
        val geoShape = result.geoShape()
        geoShape.shape().indexedShape()?.id() shouldBe "shape-1"
        geoShape.shape().indexedShape()?.index() shouldBe "shapes"
        geoShape.shape().indexedShape()?.path() shouldBe "geometry"
    }

    test("shape 정보가 없으면 geo_shape 쿼리가 생성되지 않아야 함") {
        queryOrNull {
            geoShapeQuery {
                field = "geometry"
            }
        }.shouldBeNull()
    }
})
