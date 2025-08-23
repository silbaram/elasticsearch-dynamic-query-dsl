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
                mustQuery {
                    existsQuery(
                        field = "a"
                    )
                }
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()
        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.first().isExists shouldBe true
        mustQuery.first().exists().field() shouldBe "a"
    }

    test("must 쿼리에서 exists 쿼리 생성시 field가 null이면 existsQuery가 빠져서 생성되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery {
                    // 하위 쿼리에 여러개를 추가 해야 할경우 queries[...] 구문 사용
                    queries[
                        existsQuery(
                            field = null
                        ),
                        termQuery(
                            field = "a",
                            value = "1111"
                        )
                    ]
                }
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 1 // termQuery만 추가되어 크기는 1
        mustQuery.first().isTerm shouldBe true
        mustQuery.first().term().field() shouldBe "a"
    }

    test("exists 쿼리에 boost 설정시 적용이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery {
                    existsQuery(
                        field = "a",
                        boost = 2.0F
                    )
                }
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isExists }.find { it.exists().field() == "a" }?.exists()?.field() shouldBe "a"
        mustQuery.filter { it.isExists }.find { it.exists().field() == "a" }?.exists()?.boost() shouldBe 2.0F
    }
})