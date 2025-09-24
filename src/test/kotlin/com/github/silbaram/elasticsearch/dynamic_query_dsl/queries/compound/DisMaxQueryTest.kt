package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class DisMaxQueryTest : FunSpec({

    test("dis_max 쿼리가 tie_breaker, boost, _name을 반영해야 함") {
        val result = query {
            disMaxQuery {
                tieBreaker = 0.4
                boost = 1.2f
                _name = "dismax"
                queries {
                    query { matchQuery { field = "title"; query = "elastic" } }
                    query { termQuery { field = "status"; value = "published" } }
                }
            }
        }

        result.isDisMax shouldBe true
        val disMax = result.disMax()
        disMax.tieBreaker() shouldBe 0.4
        disMax.boost() shouldBe 1.2f
        disMax.queryName() shouldBe "dismax"
        disMax.queries().shouldHaveSize(2)
        disMax.queries()[0].isMatch shouldBe true
        disMax.queries()[1].isTerm shouldBe true
    }

    test("쿼리가 없으면 dis_max 쿼리를 생성하지 않아야 함") {
        queryOrNull {
            disMaxQuery {
                tieBreaker = 0.2
            }
        }.shouldBeNull()
    }
})

