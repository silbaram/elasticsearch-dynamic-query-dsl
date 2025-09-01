package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.rangeQuery
import io.kotest.assertions.print.print
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class RangeQueryTest: FunSpec ({

    test("must 쿼리에서 range 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery {
                    queries[
                        rangeQuery(
                            field = "a",
                            from = "1234"
                        ),
                        rangeQuery(
                            field = "a",
                            to = "5678"
                        ),
                        rangeQuery(
                            field = "b",
                            gt = 123,
                            lt = 567
                        ),
                        rangeQuery(
                            field = "c",
                            gte = 456,
                            lte = 789
                        )
                    ]
                }
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 4

        mustQuery.filter { it.isRange && it.range().field() == "a"}.size shouldBe 2
        mustQuery.filter { it.isRange && it.range().field() == "b"}.size shouldBe 1
        mustQuery.filter { it.isRange && it.range().field() == "c"}.size shouldBe 1


        mustQuery.filter { it.isRange && it.range().field() == "a" && it.range().from() != null}[0].range().from().print().value shouldBe "\"1234\""
        mustQuery.filter { it.isRange && it.range().field() == "a" && it.range().to() != null}[0].range().to().print().value shouldBe "\"5678\""
        mustQuery.filter { it.isRange && it.range().field() == "b"}[0].range().gt().print().value shouldBe "123"
        mustQuery.filter { it.isRange && it.range().field() == "b"}[0].range().lt().print().value shouldBe "567"
        mustQuery.filter { it.isRange && it.range().field() == "c"}[0].range().gte().print().value shouldBe "456"
        mustQuery.filter { it.isRange && it.range().field() == "c"}[0].range().lte().print().value shouldBe "789"
    }

    test("must 쿼리에서 rangeQuery에 boost 설정시 적용이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery {
                    rangeQuery(
                        field = "d",
                        gte = 10,
                        lte = 20,
                        boost = 3.0F
                    )
                }
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isRange }.find { it.range().field() == "d" }?.range()?.boost() shouldBe 3.0F
    }

    test("range 쿼리에 _name이 설정되면 range.queryName에 반영되어야함") {
        val boolQuery = Query.Builder().boolQuery {
            mustQuery {
                rangeQuery(
                    field = "d",
                    gte = 10,
                    lte = 20,
                    _name = "named"
                )
            }
        }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isRange }.find { it.range().field() == "d" }!!.range().queryName() shouldBe "named"
    }
})
