package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SpanQueriesTest : FunSpec({

    test("span_containing: 기본 동작 및 속성 확인") {
        val little = query { spanTermQuery { field = "body"; value = "apple" } }
        val big = query {
            spanNearQuery {
                clauses[
                    { spanTermQuery { field = "body"; value = "green" } },
                    { spanTermQuery { field = "body"; value = "apple" } }
                ]
                slop = 3
                inOrder = true
            }
        }

        val q = query {
            spanContainingQuery {
                little { +little }
                big { +big }
                boost = 1.2f
                _name = "contains-apple"
            }
        }

        q shouldNotBe null
        q.isSpanContaining shouldBe true
        val sc = q.spanContaining()
        sc.boost() shouldBe 1.2f
        sc.queryName() shouldBe "contains-apple"

        // little과 big은 이제 SpanQuery로 변환되어 전달됨
        sc.little().isSpanTerm shouldBe true
        sc.big().isSpanNear shouldBe true

        val near = sc.big().spanNear()
        near.slop() shouldBe 3
        near.inOrder() shouldBe true
        near.clauses().size shouldBe 2
        near.clauses()[0].isSpanTerm shouldBe true
    }

    test("span_containing: null 입력 시 쿼리 생략") {
        // 1. 내부 쿼리가 null을 반환하는 경우
        val q1 = com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull {
            spanContainingQuery {
                little { /* none */ }
                big { spanNearQuery { clauses[ { spanTermQuery { field = "body"; value = "x" } } ]; slop = 0 } }
            }
        }
        q1 shouldBe null

        // 2. 인자 자체가 null인 경우
        val q2 = com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull {
            spanContainingQuery {
                little { spanTermQuery { field = "body"; value = "apple" } }
                big { /* none */ }
            }
        }
        q2 shouldBe null
    }

    test("span_near: 잘못된 파라미터(빈 clauses, 음수 slop) 생략") {
        // clauses가 비어있거나 null만 포함
        val invalid1 = com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull {
            spanNearQuery { slop = 1 }
        }
        invalid1 shouldBe null

        val invalid2 = com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull {
            spanNearQuery { slop = 1; clauses[ null, null ] }
        }
        invalid2 shouldBe null

        // slop이 음수
        val invalid3 = com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull {
            spanNearQuery { slop = -1; clauses[ { spanTermQuery { field = "f"; value = "v" } } ] }
        }
        invalid3 shouldBe null
    }

    test("span 쿼리가 bool 쿼리 내에서 정상적으로 조합되어야 한다") {
        val finalQuery = query {
            boolQuery {
                mustQuery { spanTermQuery { field = "field1"; value = "value1"; _name = "my_span_term" } }
                mustQuery {
                    spanContainingQuery {
                        little { spanTermQuery { field = "f1"; value = "v1" } }
                        big { spanNearQuery { clauses[ { spanTermQuery { field = "f1"; value = "v2" } } ]; slop = 2 } }
                        _name = "my_span_containing"
                    }
                }
            }
        }

        finalQuery.isBool shouldBe true
        val bool = finalQuery.bool()
        bool.must().size shouldBe 2
        bool.must()[0].isSpanTerm shouldBe true
        bool.must()[0].spanTerm().queryName() shouldBe "my_span_term"
        bool.must()[1].isSpanContaining shouldBe true
        bool.must()[1].spanContaining().queryName() shouldBe "my_span_containing"
    }
})
