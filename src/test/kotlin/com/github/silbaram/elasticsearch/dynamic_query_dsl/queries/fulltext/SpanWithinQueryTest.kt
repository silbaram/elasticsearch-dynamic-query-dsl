package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SpanWithinQueryTest : FunSpec({

    test("span_within: 기본 동작 및 속성 확인") {
        val little = spanTermQuery("body", "green")
        val big = spanNearQuery(
            clauses = listOf(
                spanTermQuery("body", "green"),
                spanTermQuery("body", "apple")
            ),
            slop = 2,
            inOrder = true
        )

        val q = spanWithinQuery(little = little, big = big, boost = 1.2f, _name = "within_green")
        q shouldNotBe null
        q!!.isSpanWithin shouldBe true
        val sw = q.spanWithin()
        sw.boost() shouldBe 1.2f
        sw.queryName() shouldBe "within_green"
        sw.little().isSpanTerm shouldBe true
        sw.big().isSpanNear shouldBe true
    }

    test("span_within: 블록 DSL 동작 및 no-op fallback") {
        val q = com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query {
            spanWithinQuery {
                little { spanTermQuery("body", "green") }
                big {
                    spanNearQuery(
                        clauses = listOf(
                            spanTermQuery("body", "green"),
                            spanTermQuery("body", "apple")
                        ),
                        slop = 1
                    )
                }
                _name = "within_dsl"
            }
        }
        q.isSpanWithin shouldBe true
        q.spanWithin().queryName() shouldBe "within_dsl"

        val fallback = com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query {
            spanWithinQuery {
                // little/big 미설정 → no-op
            }
            matchAll { it }
        }
        fallback.isMatchAll shouldBe true
    }

    test("span_within: null/비-span 입력 시 null 반환") {
        val q1 = spanWithinQuery(little = null, big = spanTermQuery("f", "v"))
        q1 shouldBe null

        val q2 = spanWithinQuery(little = spanTermQuery("f", "v"), big = null)
        q2 shouldBe null

        val q3 = spanWithinQuery(little = matchQuery("f", "v"), big = spanTermQuery("f", "v"))
        q3 shouldBe null
    }
})
