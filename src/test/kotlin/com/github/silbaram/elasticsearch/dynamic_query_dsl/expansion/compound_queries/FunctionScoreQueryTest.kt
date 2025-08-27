package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level.functionScoreQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.query
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FunctionScoreQueryTest : FunSpec({

    test("최상위 function_score 쿼리가 올바르게 생성되어야 한다") {
        val q = query {
            functionScoreQuery {
                query {
                    termQuery(field = "title", value = "apple")
                }
                functions {
                    weight(2.0)
                }
                boostMode = FunctionBoostMode.Multiply
                scoreMode = FunctionScoreMode.Sum
                boost = 1.2f
            }
        }

        q.isFunctionScore shouldBe true
        val fs = q.functionScore()
        fs.query()?.isTerm shouldBe true
        fs.functions().size shouldBe 1
        fs.functions().first().weight() shouldBe 2.0
        fs.boostMode() shouldBe FunctionBoostMode.Multiply
        fs.scoreMode() shouldBe FunctionScoreMode.Sum
        fs.boost() shouldBe 1.2f
    }

    test("function_score는 bool 쿼리의 각 절에서 사용될 수 있다") {
        val q = query {
            boolQuery {
                mustQuery {
                    functionScoreQuery {
                        query { termQuery("title", "apple") }
                        functions { weight(2.0) }
                    }
                }
                filterQuery {
                    functionScoreQuery {
                        query { termQuery("tag", "tech") }
                        functions { weight(3.0) }
                    }
                }
                mustNotQuery {
                    functionScoreQuery {
                        query { termQuery("status", "inactive") }
                        functions { weight(1.0) }
                    }
                }
            }
        }

        q.isBool shouldBe true
        q.bool().must().size shouldBe 1
        q.bool().filter().size shouldBe 1
        q.bool().mustNot().size shouldBe 1

        q.bool().must().first().functionScore().functions().first().weight() shouldBe 2.0
        q.bool().filter().first().functionScore().functions().first().weight() shouldBe 3.0
        q.bool().mustNot().first().functionScore().functions().first().weight() shouldBe 1.0
    }

    test("함수가 비어있을 때 최상위 function_score는 건너뛰어야 한다") {
        shouldThrow<Exception> {
            query {
                functionScoreQuery {
                    query { termQuery("title", "apple") }
                }
            }
        }
    }

    test("field_value_factor 함수가 적용되어야 한다") {
        val q = query {
            functionScoreQuery {
                query { termQuery("title", "apple") }
                functions {
                    fieldValueFactor(
                        field = "likes",
                        factor = 1.2,
                        missing = 1.0,
                    )
                }
            }
        }

        q.isFunctionScore shouldBe true
        val function = q.functionScore().functions().first()
        function.fieldValueFactor().field() shouldBe "likes"
        function.fieldValueFactor().factor() shouldBe 1.2
        function.fieldValueFactor().missing() shouldBe 1.0
    }
})

