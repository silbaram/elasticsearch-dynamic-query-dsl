package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode
import co.elastic.clients.json.JsonData
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class FunctionScoreTest : FunSpec({

    test("function_score: 기본 field_value_factor 테스트") {
        val functionScore = query {
            functionScoreQuery {
                query { termQuery { field = "status"; value = "active" } }
                function {
                    fieldValueFactorQuery(
                        field = "rating",
                        modifier = "ln2p",
                        factor = 1.5
                    )
                }
                scoreMode = FunctionScoreMode.Sum
                boostMode = FunctionBoostMode.Multiply
            }
        }
        
        functionScore.isFunctionScore shouldBe true
        functionScore.functionScore().functions().size shouldBe 1
        functionScore.functionScore().functions()[0].fieldValueFactor().field() shouldBe "rating"
        functionScore.functionScore().scoreMode() shouldBe FunctionScoreMode.Sum
        functionScore.functionScore().boostMode() shouldBe FunctionBoostMode.Multiply
    }

    test("function_score: weight 함수 테스트") {
        val q = query {
            functionScoreQuery {
                query { termQuery { field = "featured"; value = "true" } }
                function {
                    weightQuery(1.5)
                }
            }
        }

        q.isFunctionScore shouldBe true
        val func = q.functionScore().functions()[0]
        func.weight() shouldBe 1.5
    }

    test("function_score: script_score 테스트") {
        val q = query {
            functionScoreQuery {
                query { termQuery { field = "category"; value = "electronics" } }
                function {
                    scriptScoreQuery(
                        source = "Math.log(2 + doc['rating'].value) * params.factor",
                        params = mapOf("factor" to JsonData.of(1.2))
                    )
                }
                boostMode = FunctionBoostMode.Replace
            }
        }

        q.isFunctionScore shouldBe true
        val fsq = q.functionScore()
        fsq.functions().size shouldBe 1
        val scriptScore = fsq.functions()[0].scriptScore()
        scriptScore shouldNotBe null
        scriptScore.script().inline().source() shouldBe "Math.log(2 + doc['rating'].value) * params.factor"
        fsq.boostMode() shouldBe FunctionBoostMode.Replace
    }

    test("function_score: random_score 테스트") {
        val q = query {
            functionScoreQuery {
                query { termQuery { field = "type"; value = "product" } }
                function {
                    randomScoreQuery(seed = "random123", field = "user_id")
                }
                boostMode = FunctionBoostMode.Replace
            }
        }

        q.isFunctionScore shouldBe true
        val fsq = q.functionScore()
        fsq.functions().size shouldBe 1
        val randomScore = fsq.functions()[0].randomScore()
        randomScore shouldNotBe null
        randomScore.seed() shouldBe "random123"
        randomScore.field() shouldBe "user_id"
        fsq.boostMode() shouldBe FunctionBoostMode.Replace
    }

    test("function_score: 문자열 기반 mode 설정 테스트") {
        val dsl = FunctionScoreQueryDsl()
        dsl.scoreMode("avg")
        dsl.boostMode("sum")
        
        dsl.scoreMode shouldBe FunctionScoreMode.Avg
        dsl.boostMode shouldBe FunctionBoostMode.Sum
    }

    test("function_score: 동적 제외 테스트 - null 필드는 함수 생성 안됨") {
        val q = query {
            functionScoreQuery {
                query { termQuery { field = "status"; value = "active" } }
                function {
                    fieldValueFactorQuery(field = null) // 이 함수는 생성되지 않아야 함
                }
                function {
                    fieldValueFactorQuery(field = "rating", factor = 2.0) // 이 함수만 생성됨
                }
            }
        }

        q.isFunctionScore shouldBe true
        val fsq = q.functionScore()
        fsq.functions().size shouldBe 1 // null 필드 함수는 제외됨
        fsq.functions()[0].fieldValueFactor().field() shouldBe "rating"
    }

    test("function_score: missing 값 처리 테스트") {
        val q = query {
            functionScoreQuery {
                query { termQuery { field = "type"; value = "article" } }
                function {
                    fieldValueFactorQuery(
                        field = "view_count",
                        factor = 1.5,
                        missing = 1.0  // view_count가 없는 문서는 1.0으로 처리
                    )
                }
            }
        }

        q.isFunctionScore shouldBe true
        val fsq = q.functionScore()
        fsq.functions().size shouldBe 1
        val fieldValueFactor = fsq.functions()[0].fieldValueFactor()
        fieldValueFactor.field() shouldBe "view_count"
        fieldValueFactor.factor() shouldBe 1.5
        fieldValueFactor.missing() shouldBe 1.0
    }

    // --- Kibana Parity tests merged from FunctionScoreKibanaParityTest ---
    test("function_score 별칭 API와 filterQuery DSL이 Kibana와 유사하게 동작해야 한다") {
        val query = query {
            functionScoreQuery {
                // 기본 query
                query { termQuery { field = "title"; value = "kotlin" } }
                // 함수 1: field_value_factor + filter
                function {
                    fieldValueFactor(field = "rating", modifier = "log1p", factor = 1.2, missing = 1.0)
                    filterQuery { termQuery { field = "status"; value = "active" } }
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
        // 함수 2 확인
        val f2 = fs.functions()[1]
        f2.weight() shouldBe 0.5
        f2.randomScore().seed() shouldBe "seed-1"
    }

    test("function_score: script_score 별칭 API 동작 확인") {
        val query = query {
            functionScoreQuery {
                query { termQuery { field = "category"; value = "electronics" } }
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
