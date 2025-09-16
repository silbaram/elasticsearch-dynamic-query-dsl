package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SpanWithinQueryTest : FunSpec({

    test("span_within: 기본 동작 및 속성 확인") {
        val little = query { spanTermQuery { field = "body"; value = "green" } }
        val big = query {
            spanNearQuery {
                clauses[
                    { spanTermQuery { field = "body"; value = "green" } },
                    { spanTermQuery { field = "body"; value = "apple" } }
                ]
                slop = 2
                inOrder = true
            }
        }

        val q = query {
            spanWithinQuery {
                this.little { +little }
                this.big { +big }
                boost = 1.2f
                _name = "within_green"
            }
        }
        q shouldNotBe null
        q.isSpanWithin shouldBe true
        val sw = q.spanWithin()
        sw.boost() shouldBe 1.2f
        sw.queryName() shouldBe "within_green"
        sw.little().isSpanTerm shouldBe true
        sw.big().isSpanNear shouldBe true
    }

    test("span_within: 블록 DSL 동작 및 no-op fallback") {
        val q = com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query {
            spanWithinQuery {
                little { spanTermQuery { field = "body"; value = "green" } }
                big {
                    spanNearQuery {
                        clauses[
                            { spanTermQuery { field = "body"; value = "green" } },
                            { spanTermQuery { field = "body"; value = "apple" } }
                        ]
                        slop = 1
                    }
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
        val q1 = queryOrNull {
            spanWithinQuery {
                little { /* none */ }
                big { +query { spanTermQuery { field = "f"; value = "v" } } }
            }
        }
        q1 shouldBe null

        val q2 = queryOrNull {
            spanWithinQuery {
                little { +query { spanTermQuery { field = "f"; value = "v" } } }
                big { /* none */ }
            }
        }
        q2 shouldBe null

        val q3 = queryOrNull {
            spanWithinQuery {
                little { +com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query { matchQuery { field = "f"; query = "v" } } }
                big { +query { spanTermQuery { field = "f"; value = "v" } } }
            }
        }
        q3 shouldBe null
    }
})
