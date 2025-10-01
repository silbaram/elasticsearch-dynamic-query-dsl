package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PrefixQueryTest : FunSpec({

    test("prefix 쿼리가 must 절에 포함되어야 함") {
        val result = query {
            boolQuery {
                mustQuery {
                    prefixQuery { field = "title"; value = "pre"; caseInsensitive = true; boost = 2.1f }
                }
            }
        }

        val must = result.bool().must()

        result.isBool shouldBe true
        must.size shouldBe 1
        val prefix = must.first()
        prefix.isPrefix shouldBe true
        prefix.prefix().field() shouldBe "title"
        prefix.prefix().value() shouldBe "pre"
        prefix.prefix().caseInsensitive() shouldBe true
        prefix.prefix().boost() shouldBe 2.1f
    }

    test("필드나 값이 비어있으면 prefix 쿼리가 생성되지 않아야 함") {
        val result = query {
            boolQuery {
                mustQuery {
                    prefixQuery { field = ""; value = "pre" }
                    prefixQuery { field = "title"; value = null }
                }
            }
        }

        result.bool().must().isEmpty() shouldBe true
    }
})
