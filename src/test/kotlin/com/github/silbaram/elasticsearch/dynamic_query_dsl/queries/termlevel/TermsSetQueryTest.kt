package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import co.elastic.clients.elasticsearch._types.Script
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TermsSetQueryTest : FunSpec({

    test("terms_set 쿼리가 필수 속성과 함께 생성되어야 함") {
        val result = query {
            boolQuery {
                mustQuery {
                    termsSetQuery {
                        field = "tags"
                        terms = listOf("tech", "kotlin", "search")
                        minimumShouldMatchField = "required_matches"
                        minimumShouldMatchScript = Script.of { script ->
                            script.inline { inlineScript ->
                                inlineScript.source("Math.min(params.num_terms, 2)")
                            }
                        }
                    }
                }
            }
        }

        val must = result.bool().must()

        must.size shouldBe 1
        val termsSet = must.first()
        termsSet.isTermsSet shouldBe true
        termsSet.termsSet().field() shouldBe "tags"
        termsSet.termsSet().terms() shouldBe listOf("tech", "kotlin", "search")
        termsSet.termsSet().minimumShouldMatchField() shouldBe "required_matches"
        termsSet.termsSet().minimumShouldMatchScript()?.isInline shouldBe true
        termsSet.termsSet().minimumShouldMatchScript()?.inline()?.source() shouldBe "Math.min(params.num_terms, 2)"
    }

    test("필드가 없거나 terms가 비면 terms_set 쿼리를 생성하지 않아야 함") {
        val result = query {
            boolQuery {
                mustQuery {
                    termsSetQuery { field = null; terms = listOf("a") }
                    termsSetQuery { field = "tags"; terms = emptyList() }
                }
            }
        }

        result.bool().must().isEmpty() shouldBe true
    }
})
