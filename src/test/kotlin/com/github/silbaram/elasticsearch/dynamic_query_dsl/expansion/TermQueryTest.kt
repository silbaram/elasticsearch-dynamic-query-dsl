package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.query.termQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TermQueryTest: FunSpec({

    test("bool 밑에 must query 생성 테스트") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
                    termQuery(
                        field = "a",
                        value = "1111"
                    ),
                    termQuery(
                        field = "b",
                        value = "2222"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 2
        mustQuery.filter { it.isTerm }.find { it.term().field() == "a" }?.term()?.value()?.stringValue() shouldBe "1111"
        mustQuery.filter { it.isTerm }.find { it.term().field() == "b" }?.term()?.value()?.stringValue() shouldBe "2222"
    }

    test("termQuery에 value 값이 비었거나 null면 제외 테스트") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
                    termQuery(
                        field = "a",
                        value = null
                    ),
                    termQuery(
                        field = "a",
                        value = ""
                    ),
                    termQuery(
                        field = "c",
                        value = "3333"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        println(boolQueryBuild)
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isTerm }.find { it.term().field() == "c" }?.term()?.value()?.stringValue() shouldBe "3333"
    }
})