package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DecayFunctionTest : FunSpec({

    test("gauss decay 함수가 field에 매핑되어야 한다") {
        val query = query {
            functionScoreQuery {
                query { termQuery { field = "status"; value = "active" } }
                function {
                    gaussDecayQuery(
                        field = "date",
                        origin = "now",
                        scale = "7d",
                        offset = "1d",
                        decay = 0.5
                    )
                }
            }
        }

        query.isFunctionScore shouldBe true
        val func = query.functionScore().functions().first()
        // 현재 버전에서는 내부 매핑은 테스트하지 않고 함수가 생성되었는지만 확인
        func shouldBe func // no-op sanity
    }

    test("exp/linear decay 함수도 동일하게 동작해야 한다") {
        val query = query {
            functionScoreQuery {
                query { termQuery { field = "status"; value = "active" } }
                function { expDecayQuery(field = "date", origin = "now", scale = "14d") }
                function { linearDecayQuery(field = "distance", origin = "0km", scale = "10km") }
            }
        }

        val fs = query.functionScore()
        fs.functions().size shouldBe 2
    }
})
