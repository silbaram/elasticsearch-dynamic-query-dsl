package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SpanNotQueryTest : FunSpec({

    test("span_not: 기본 동작 및 속성 확인") {
        val include = query {
            spanNearQuery {
                clauses[
                    query { spanTermQuery { field = "title"; value = "kotlin" } },
                    query { spanTermQuery { field = "title"; value = "dsl" } }
                ]
                slop = 3
                inOrder = true
            }
        }
        val exclude = query { spanTermQuery { field = "title"; value = "legacy" } }

        val q = query {
            spanNotQuery {
                include { +include }
                exclude { +exclude }
                pre = 0
                post = 1
                boost = 1.0f
                _name = "not_legacy"
            }
        }

        q shouldNotBe null
        q.isSpanNot shouldBe true
        val sn = q.spanNot()
        sn.queryName() shouldBe "not_legacy"
        sn.boost() shouldBe 1.0f
        sn.pre() shouldBe 0
        sn.post() shouldBe 1
        sn.include().isSpanNear shouldBe true
        sn.exclude().isSpanTerm shouldBe true
    }

    test("span_not: DSL no-op이면 fallback 유지") {
        val q = query {
            spanNotQuery {
                // include 미제공 → no-op
                exclude { query { spanTermQuery { field = "f"; value = "v" } } }
                pre = -1 // 무시
            }
            matchAll { it } // fallback 유지
        }

        q.isMatchAll shouldBe true
    }

    test("span_not: include 또는 exclude 가 null이면 null 반환") {
        val ex = query { spanTermQuery { field = "f"; value = "v" } }
        val q1 = queryOrNull {
            spanNotQuery {
                include { /* none */ }
                exclude { +ex }
            }
        }
        q1 shouldBe null

        val inc = query { spanTermQuery { field = "f"; value = "v" } }
        val q2 = queryOrNull {
            spanNotQuery {
                include { +inc }
                exclude { /* none */ }
            }
        }
        q2 shouldBe null
    }

    test("span_not: include/exclude 에 비-span 전달 시 생략") {
        val nonSpan = com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query { matchQuery { field = "title"; query = "text" } }
        val ex2 = query { spanTermQuery { field = "title"; value = "legacy" } }
        val qx = queryOrNull {
            spanNotQuery {
                include { +nonSpan }
                exclude { +ex2 }
            }
        }
        qx shouldBe null
    }

    test("span_not: 중첩 span_not 허용") {
        val inner = query {
            spanNotQuery {
                include { spanTermQuery { field = "body"; value = "green" } }
                exclude { spanTermQuery { field = "body"; value = "apple" } }
            }
        }

        val outer = query {
            spanNotQuery {
                include { spanTermQuery { field = "body"; value = "fruit" } }
                exclude { +inner }
            }
        }

        outer.isSpanNot shouldBe true
        val sn = outer.spanNot()
        sn.include().isSpanTerm shouldBe true
        sn.exclude().isSpanNot shouldBe true
    }
})
