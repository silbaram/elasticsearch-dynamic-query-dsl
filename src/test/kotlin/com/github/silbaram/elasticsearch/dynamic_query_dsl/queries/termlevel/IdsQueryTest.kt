package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class IdsQueryTest : FunSpec({

    test("ids 쿼리가 filter 절에 포함되어야 함") {
        val result = query {
            boolQuery {
                filterQuery {
                    idsQuery { values = listOf("1", "2", "3"); boost = 1.5f; _name = "ids-filter" }
                }
            }
        }

        val filters = result.bool().filter()

        filters.size shouldBe 1
        val ids = filters.first()
        ids.isIds shouldBe true
        ids.ids().values() shouldBe listOf("1", "2", "3")
        ids.ids().boost() shouldBe 1.5f
        ids.ids().queryName() shouldBe "ids-filter"
    }

    test("값이 없으면 ids 쿼리가 생성되지 않아야 함") {
        val result = query {
            boolQuery {
                filterQuery {
                    idsQuery { values = emptyList() }
                }
            }
        }

        result.bool().filter().isEmpty() shouldBe true
    }
})
