package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SpanContainingDslTest : FunSpec({
    test("span_containing: 블록 DSL 동작 및 속성 확인") {
        val q = query {
            spanContainingQuery {
                little { spanTermQuery { field = "body"; value = "green" } }
                big {
                    spanNearQuery {
                        clauses[
                            { spanTermQuery { field = "body"; value = "green" } },
                            { spanTermQuery { field = "body"; value = "apple" } }
                        ]
                        slop = 1
                        inOrder = true
                    }
                }
                _name = "containing_dsl"
            }
        }

        q.isSpanContaining shouldBe true
        q.spanContaining().queryName() shouldBe "containing_dsl"
        q.spanContaining().little().isSpanTerm shouldBe true
        q.spanContaining().big().isSpanNear shouldBe true
    }
})
