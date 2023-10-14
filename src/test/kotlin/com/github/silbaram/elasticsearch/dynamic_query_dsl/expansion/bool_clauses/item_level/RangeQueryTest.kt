package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries.boolQuery
import io.kotest.assertions.print.print
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RangeQueryTest: FunSpec ({

    test("must 쿼리에서 range 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
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
                )
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
})