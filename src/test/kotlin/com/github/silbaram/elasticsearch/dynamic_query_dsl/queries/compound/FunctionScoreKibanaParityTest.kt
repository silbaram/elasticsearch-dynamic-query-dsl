package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode
import co.elastic.clients.json.JsonData
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FunctionScoreKibanaParityTest : FunSpec({

    test("function_score 별칭 API와 filterQuery DSL이 Kibana와 유사하게 동작해야 한다") {
        val query = query {
            functionScoreQuery {
                // 기본 query
                query { termQuery("title", "kotlin") }
                // 함수 1: field_value_factor + filter
                function {
                    fieldValueFactor(field = "rating", modifier = "log1p", factor = 1.2, missing = 1.0)
                    filterQuery { termQuery("status", "active") }
                }
                // 함수 2: weight + random_score 별칭
                function {
                    weight(0.5)
                    randomScore(seed = "seed-1", field = "user_id")
                }
                // 문자열 기반 모드 설정
                scoreMode("sum")
                boostMode("multiply")
            }
        }

        query.isFunctionScore shouldBe true
        val fs = query.functionScore()
        fs.scoreMode() shouldBe FunctionScoreMode.Sum
        fs.boostMode() shouldBe FunctionBoostMode.Multiply
        fs.functions().size shouldBe 2
        // 함수 1 확인
        val f1 = fs.functions()[0]
        f1.fieldValueFactor().field() shouldBe "rating"
        f1.filter()!!.term().field() shouldBe "status"
        // 함수 2 확인
        val f2 = fs.functions()[1]
        f2.weight() shouldBe 0.5
        f2.randomScore().seed() shouldBe "seed-1"
    }

    test("script_score 별칭 API 동작 확인") {
        val query = query {
            functionScoreQuery {
                query { termQuery("category", "electronics") }
                function {
                    scriptScore(
                        source = "doc['price'].value * params.factor",
                        params = mapOf("factor" to JsonData.of(1.1))
                    )
                }
            }
        }

        query.isFunctionScore shouldBe true
        val fs = query.functionScore()
        fs.functions().size shouldBe 1
        fs.functions().first().scriptScore().script().inline().source() shouldBe "doc['price'].value * params.factor"
    }
})
