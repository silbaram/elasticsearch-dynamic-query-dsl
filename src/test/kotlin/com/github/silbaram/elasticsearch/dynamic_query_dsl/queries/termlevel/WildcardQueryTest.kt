package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class WildcardQueryTest : FunSpec({

    test("wildcard 쿼리 패턴이 filter 절에 반영되어야 함") {
        val result = query {
            boolQuery {
                filterQuery {
                    wildcardQuery { field = "title"; value = "abc*"; caseInsensitive = true }
                }
            }
        }

        val filters = result.bool().filter()

        filters.size shouldBe 1
        val wildcard = filters.first()
        wildcard.isWildcard shouldBe true
        wildcard.wildcard().value() shouldBe "abc*"
        wildcard.wildcard().caseInsensitive() shouldBe true
    }

    test("필드나 값이 없으면 wildcard 쿼리가 제외되어야 함") {
        val result = query {
            boolQuery {
                filterQuery {
                    wildcardQuery { field = ""; value = "abc*" }
                    wildcardQuery { field = "title"; value = "" }
                }
            }
        }

        result.bool().filter().isEmpty() shouldBe true
    }
})
