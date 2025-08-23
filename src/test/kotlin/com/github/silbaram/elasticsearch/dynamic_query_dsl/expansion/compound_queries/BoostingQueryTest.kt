package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level.termQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BoostingQueryTest : FunSpec({

    test("boosting query가 추가 되어야함") {
        val boostingQuery = boostingQuery {
            positive {
                queries[
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                ]
            }
            negative {
                queries[
                    termQuery(
                        field = "field2",
                        value = "value2"
                    )
                ]
            }
            negativeBoost = 0.2
        }

        boostingQuery.isBoosting shouldBe true
        boostingQuery.boosting().positive().isTerm shouldBe true
        boostingQuery.boosting().positive().term().field() shouldBe "field1"
        boostingQuery.boosting().positive().term().value().stringValue() shouldBe "value1"

        boostingQuery.boosting().negative().isTerm shouldBe true
        boostingQuery.boosting().negative().term().field() shouldBe "field2"
        boostingQuery.boosting().negative().term().value().stringValue() shouldBe "value2"

        boostingQuery.boosting().negativeBoost() shouldBe 0.2
    }
})
