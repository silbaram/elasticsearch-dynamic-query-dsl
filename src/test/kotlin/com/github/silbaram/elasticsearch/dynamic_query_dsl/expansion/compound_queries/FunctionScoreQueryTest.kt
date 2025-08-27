package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FunctionScoreQueryTest : FunSpec({

    test("function_score 쿼리가 올바르게 생성되어야 한다") {
        val q = query {
            functionScoreQuery {
                query {
                    termQuery(field = "field1", value = "value1")
                }
                function {
                    weight = 2.0
                    filter {
                        termQuery(field = "field2", value = "value2")
                    }
                }
                boostMode = FunctionBoostMode.Multiply
                scoreMode = FunctionScoreMode.Sum
                maxBoost = 5.0
                minScore = 0.1
                boost = 1.5f
                _name = "fs"
            }
        }

        q.isFunctionScore shouldBe true
        val fs = q.functionScore()
        fs.query().term().field() shouldBe "field1"
        fs.functions().size shouldBe 1
        val func = fs.functions().first()
        func.weight() shouldBe 2.0
        func.filter().term().field() shouldBe "field2"
        fs.boostMode() shouldBe FunctionBoostMode.Multiply
        fs.scoreMode() shouldBe FunctionScoreMode.Sum
        fs.maxBoost() shouldBe 5.0
        fs.minScore() shouldBe 0.1
        fs.boost() shouldBe 1.5f
        fs.queryName() shouldBe "fs"
    }

    test("function_score는 bool 절 내부에서 사용될 수 있다") {
        val q = query {
            boolQuery {
                mustQuery {
                    functionScoreQuery {
                        query { termQuery("title", "elastic") }
                        function { weight = 2.0 }
                    }
                }
            }
        }

        q.bool().must().size shouldBe 1
        val fs = q.bool().must().first().functionScore()
        fs.functions().first().weight() shouldBe 2.0
    }
})

