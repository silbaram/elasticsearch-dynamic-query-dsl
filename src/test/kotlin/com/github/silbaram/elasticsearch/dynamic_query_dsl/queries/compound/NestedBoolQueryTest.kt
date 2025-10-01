package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NestedBoolQueryTest : FunSpec({

    test("mustQuery 내부에 중첩된 bool 쿼리가 올바르게 생성되어야 한다") {
        val query = query {
            boolQuery {
                mustQuery {
                    boolQuery {
                        shouldQuery {
                            termQuery { field = "tags"; value = "kotlin" }
                            termQuery { field = "tags"; value = "dsl" }
                        }
                    }
                }
            }
        }

        val mustClauses = query.bool().must()
        mustClauses.size shouldBe 1

        val nestedBool = mustClauses.first().bool()
        val nestedShouldClauses = nestedBool.should()
        nestedShouldClauses.size shouldBe 2
        nestedShouldClauses.any { it.term().field() == "tags" && it.term().value().stringValue() == "kotlin" } shouldBe true
        nestedShouldClauses.any { it.term().field() == "tags" && it.term().value().stringValue() == "dsl" } shouldBe true
    }

    test("shouldQuery 내부에 중첩된 bool 쿼리가 올바르게 생성되어야 한다") {
        val query = query {
            boolQuery {
                shouldQuery {
                    boolQuery {
                        mustQuery { termQuery { field = "status"; value = "active" } }
                    }
                }
            }
        }

        val shouldClauses = query.bool().should()
        shouldClauses.size shouldBe 1

        val nestedMustClauses = shouldClauses.first().bool().must()
        nestedMustClauses.size shouldBe 1
        val termQuery = nestedMustClauses.first().term()
        termQuery.field() shouldBe "status"
        termQuery.value().stringValue() shouldBe "active"
    }

    test("하나의 절에 여러 개의 중첩 bool 쿼리를 추가할 수 있어야 한다") {
        val query = query {
            boolQuery {
                mustQuery {
                    boolQuery { shouldQuery { termQuery { field = "field1"; value = "A" } } }
                    boolQuery { mustQuery { termQuery { field = "field2"; value = "B" } } }
                }
            }
        }

        val mustClauses = query.bool().must()
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
