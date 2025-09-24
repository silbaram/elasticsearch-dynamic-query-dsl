package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MatchNoneQueryTest : FunSpec({

    test("match_none 쿼리가 boost와 _name을 적용해야 함") {
        val result = query {
            matchNoneQuery {
                boost = 0.5f
                _name = "none"
            }
        }

        result.isMatchNone shouldBe true
        val matchNone = result.matchNone()
        matchNone.boost() shouldBe 0.5f
        matchNone.queryName() shouldBe "none"
    }
})

