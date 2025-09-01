package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NestedBoolQueryTest : FunSpec({

    test("mustQuery 내부에 중첩된 bool 쿼리가 올바르게 생성되어야 한다") {
        // given
        val query = Query.Builder()
            .boolQuery { // 최상위 bool
                mustQuery {
                    // 중첩 bool 쿼리 생성
                    boolQuery {
                        shouldQuery {
                            queries[
                                termQuery(field = "tags", value = "kotlin"),
                                termQuery(field = "tags", value = "dsl")
                            ]
                        }
                    }
                }
            }
            .build()

        // when
        val topLevelBool = query.bool()
        val mustClauses = topLevelBool.must()

        // then
        mustClauses.size shouldBe 1
        val nestedQuery = mustClauses.first()

        nestedQuery.isBool shouldBe true
        val nestedBool = nestedQuery.bool()
        val nestedShouldClauses = nestedBool.should()

        nestedShouldClauses.size shouldBe 2
        nestedShouldClauses.any { it.term().field() == "tags" && it.term().value().stringValue() == "kotlin" } shouldBe true
        nestedShouldClauses.any { it.term().field() == "tags" && it.term().value().stringValue() == "dsl" } shouldBe true
    }

    test("shouldQuery 내부에 중첩된 bool 쿼리가 올바르게 생성되어야 한다") {
        // given
        val query = Query.Builder()
            .boolQuery { // 최상위 bool
                shouldQuery {
                    // 중첩 bool 쿼리 생성
                    boolQuery {
                        mustQuery {
                            termQuery(field = "status", value = "active")
                        }
                    }
                }
            }
            .build()

        // when
        val topLevelBool = query.bool()
        val shouldClauses = topLevelBool.should()

        // then
        shouldClauses.size shouldBe 1
        val nestedQuery = shouldClauses.first()

        nestedQuery.isBool shouldBe true
        val nestedBool = nestedQuery.bool()
        val nestedMustClauses = nestedBool.must()

        nestedMustClauses.size shouldBe 1
        val termQuery = nestedMustClauses.first().term()
        termQuery.field() shouldBe "status"
        termQuery.value().stringValue() shouldBe "active"
    }

    test("하나의 절에 여러 개의 중첩 bool 쿼리를 추가할 수 있어야 한다") {
        // given
        val query = Query.Builder()
            .boolQuery {
                mustQuery {
                    queries[
                        // 첫 번째 중첩 bool
                        boolQuery {
                            shouldQuery { termQuery("field1", "A") }
                        },
                        // 두 번째 중첩 bool
                        boolQuery {
                            mustQuery { termQuery("field2", "B") }
                        }
                    ]
                }
            }
            .build()

        // when
        val mustClauses = query.bool().must()

        // then
        mustClauses.size shouldBe 2
        mustClauses.all { it.isBool } shouldBe true

        val firstNestedBool = mustClauses[0].bool()
        firstNestedBool.should().size shouldBe 1
        firstNestedBool.should().first().term().field() shouldBe "field1"

        val secondNestedBool = mustClauses[1].bool()
        secondNestedBool.must().size shouldBe 1
        secondNestedBool.must().first().term().field() shouldBe "field2"
    }
})
