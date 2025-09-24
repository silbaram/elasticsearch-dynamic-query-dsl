package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.geo

import co.elastic.clients.elasticsearch._types.GeoShapeRelation
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class ShapeQueryTest : FunSpec({

    test("shape 쿼리가 inline WKT와 relation을 반영해야 함") {
        val wkt = "LINESTRING (30 10, 10 30, 40 40)"

        val result = query {
            shapeQuery {
                field = "geometry"
                relation = GeoShapeRelation.Intersects
                ignoreUnmapped = true
                shape(mapOf("type" to "wkt", "value" to wkt))
            }
        }

        result.isShape shouldBe true
        val shape = result.shape()
        shape.field() shouldBe "geometry"
        shape.ignoreUnmapped() shouldBe true
        shape.shape().relation() shouldBe GeoShapeRelation.Intersects
        shape.shape().shape()?.toString()!!.contains("LINESTRING") shouldBe true
    }

    test("indexed_shape를 사용한 shape 쿼리가 생성되어야 함") {
        val result = query {
            shapeQuery {
                field = "geometry"
                indexedShape {
                    id = "shape-2"
                    index = "shapes"
                    path = "geometry"
                    routing = "route"
                }
            }
        }

        result.isShape shouldBe true
        val shape = result.shape()
        shape.shape().indexedShape()?.id() shouldBe "shape-2"
        shape.shape().indexedShape()?.index() shouldBe "shapes"
        shape.shape().indexedShape()?.path() shouldBe "geometry"
        shape.shape().indexedShape()?.routing() shouldBe "route"
    }

    test("shape 데이터가 없으면 shape 쿼리가 생성되지 않아야 함") {
        queryOrNull {
            shapeQuery {
                field = "geometry"
            }
        }.shouldBeNull()
    }
})
