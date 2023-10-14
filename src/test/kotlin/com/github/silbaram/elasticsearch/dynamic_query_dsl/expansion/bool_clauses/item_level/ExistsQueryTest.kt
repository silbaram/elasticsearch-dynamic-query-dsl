package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ExistsQueryTest: FunSpec ({

    test("must 쿼리에서 exists 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
                    existsQuery(
                        field = "a"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isExists }.find { it.exists().field() == "a" }!!.exists().field() shouldBe "a"
    }

    test("must 쿼리에서 exists 쿼리 생성시 field가 null이면 existsQuery가 빠져서 생성되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
                    existsQuery(
                        field = null
                    ),
                    termQuery(
                        field = "a",
                        value = "1111"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isExists }.size shouldBe 0
    }
})