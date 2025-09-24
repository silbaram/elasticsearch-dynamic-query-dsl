package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FuzzyQueryTest : FunSpec({

    test("fuzzy 쿼리가 문자열 값으로 생성되어야 함") {
        val result = query {
            boolQuery {
                mustQuery {
                    queries[
                        { fuzzyQuery { field = "name"; value = "kim"; fuzziness = "AUTO"; prefixLength = 1; maxExpansions = 20; transpositions = true } }
                    ]
                }
            }
        }

        val must = result.bool().must()

        must.size shouldBe 1
        val fuzzy = must.first()
        fuzzy.isFuzzy shouldBe true
        fuzzy.fuzzy().value().stringValue() shouldBe "kim"
        fuzzy.fuzzy().fuzziness() shouldBe "AUTO"
        fuzzy.fuzzy().prefixLength() shouldBe 1
        fuzzy.fuzzy().maxExpansions() shouldBe 20
        fuzzy.fuzzy().transpositions() shouldBe true
    }

    test("숫자 값을 fuzzy 쿼리로 변환할 수 있어야 함") {
        val result = query {
            boolQuery {
                mustQuery {
                    queries[
                        { fuzzyQuery { field = "age"; value = 33; fuzziness = "1" } }
                    ]
                }
            }
        }
        val must = result.bool().must()

        must.size shouldBe 1
        val fuzzy = must.first().fuzzy()
        fuzzy.field() shouldBe "age"
        fuzzy.value().longValue() shouldBe 33L
    }

    test("필드가 없거나 값이 null이면 fuzzy 쿼리를 생성하지 않아야 함") {
        val result = query {
            boolQuery {
                mustQuery {
                    queries[
                        { fuzzyQuery { field = ""; value = "kim" } },
                        { fuzzyQuery { field = "name"; value = null } }
                    ]
                }
            }
        }

        result.bool().must().isEmpty() shouldBe true
    }
})
