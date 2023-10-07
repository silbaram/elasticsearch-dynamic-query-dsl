package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import io.kotest.core.spec.style.FunSpec

class RangeQueryTest: FunSpec ({

    test("must 쿼리에서 range 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
                    rangeQuery(
                        field = "a",
                        from = "1234"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
println(boolQueryBuild)
//        val mustQuery = boolQueryBuild.bool().must()
//
//        boolQueryBuild.isBool shouldBe true
//        mustQuery.size shouldBe 2
//        mustQuery.filter { it.isTerm }.find { it.term().field() == "a" }?.term()?.value()?.stringValue() shouldBe "1111"
//        mustQuery.filter { it.isTerm }.find { it.term().field() == "b" }?.term()?.value()?.stringValue() shouldBe "2222"
    }
})