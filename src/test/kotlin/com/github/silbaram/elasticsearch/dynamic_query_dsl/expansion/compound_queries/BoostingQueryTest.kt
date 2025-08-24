package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BoostingQueryTest : FunSpec({

    test("boosting query에 단일 positive, negative query가 추가되어야 함") {
        val boostingQuery = query {
            boostingQuery {
                positive {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
                negative {
                    termQuery(
                        field = "field2",
                        value = "value2"
                    )
                }
                negativeBoost = 0.2
            }
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

    test("boosting query에 여러개의 positive, negative query가 추가되어야 함") {
        val boostingQuery = query {
            boostingQuery {
                positive {
                    queries[
                        termQuery(
                            field = "field1",
                            value = "value1"
                        ),
                        termQuery(
                            field = "field1-2",
                            value = "value1-2"
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
        }

        boostingQuery.isBoosting shouldBe true
        // positive절은 bool query로 감싸져야 함
        boostingQuery.boosting().positive().isBool shouldBe true
        boostingQuery.boosting().positive().bool().must().size shouldBe 2
        // negative절은 단일 쿼리이므로 bool query로 감싸지지 않아야 함
        boostingQuery.boosting().negative().isTerm shouldBe true
        boostingQuery.boosting().negativeBoost() shouldBe 0.2
    }
})
