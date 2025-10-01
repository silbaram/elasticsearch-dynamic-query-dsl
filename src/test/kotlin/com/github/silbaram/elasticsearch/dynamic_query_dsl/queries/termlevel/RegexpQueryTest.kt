package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RegexpQueryTest : FunSpec({

    test("regexp 쿼리가 should 절에 추가되어야 함") {
        val result = query {
            boolQuery {
                shouldQuery {
                    regexpQuery { field = "keyword"; value = "ba.*"; flags = "INTERSECTION"; maxDeterminizedStates = 10000 }
                }
            }
        }

        val shouldQueries = result.bool().should()

        shouldQueries.size shouldBe 1
        val regexp = shouldQueries.first()
        regexp.isRegexp shouldBe true
        regexp.regexp().value() shouldBe "ba.*"
        regexp.regexp().flags() shouldBe "INTERSECTION"
        regexp.regexp().maxDeterminizedStates() shouldBe 10000
    }

    test("필드나 정규식이 비면 regexp 쿼리를 생성하지 않아야 함") {
        val result = query {
            boolQuery {
                shouldQuery {
                    regexpQuery { field = null; value = "ba.*" }
                    regexpQuery { field = "keyword"; value = "" }
                }
            }
        }

        result.bool().should().isEmpty() shouldBe true
    }
})
