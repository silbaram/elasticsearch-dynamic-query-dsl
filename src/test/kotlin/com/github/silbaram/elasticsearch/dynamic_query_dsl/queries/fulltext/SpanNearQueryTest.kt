package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SpanNearQueryTest : FunSpec({

    test("span_near: inOrder 미설정 시 기본 false") {
        val q = query {
            spanNearQuery {
                clause(spanTermQuery("title", "kotlin"))
                clause(spanTermQuery("title", "dsl"))
                slop = 2
                // inOrder 미설정
            }
        }

        q.isSpanNear shouldBe true
        val near = q.spanNear()
        near.slop() shouldBe 2
        // in_order 미설정 시 JSON 생략 → getter는 null일 수 있음
        near.inOrder() shouldBe null
        near.clauses().size shouldBe 2
    }

    test("span_near: DSL no-op이면 fallback 유지") {
        val q = query {
            spanNearQuery {
                // 유효하지 않은 slop
                slop = -1
            }
            // no-op 이후 fallback 으로 match_all 유지
            matchAll { it }
        }

        q.isMatchAll shouldBe true
    }

    test("span_near: 혼합 절에서 비-span 제외") {
        val q = query {
            spanNearQuery {
                slop = 1
                clauses[
                    spanTermQuery("title", "kotlin"),
                    // 비-span 쿼리 (matchQuery)는 자동 필터링되어 제외됨
                    matchQuery("title", "dsl")
                ]
            }
        }

        q.isSpanNear shouldBe true
        val near = q.spanNear()
        near.clauses().size shouldBe 1
        near.clauses()[0].isSpanTerm shouldBe true
    }

    test("span_near: 단일 절 허용 및 정상 동작") {
        val q = query {
            spanNearQuery {
                slop = 0
                clause(spanTermQuery("title", "single"))
            }
        }

        q.isSpanNear shouldBe true
        val near = q.spanNear()
        near.clauses().size shouldBe 1
        // in_order 미설정 시 JSON 생략 → getter는 null일 수 있음
        near.inOrder() shouldBe null
    }

    test("span_near: 중첩 span_near 허용") {
        val inner = spanNearQuery(
            clauses = listOf(
                spanTermQuery("title", "structured"),
                spanTermQuery("title", "concurrency")
            ),
            slop = 2
        )

        val q = query {
            spanNearQuery {
                slop = 5
                clauses[
                    spanTermQuery("title", "kotlin"),
                    inner
                ]
            }
        }

        q.isSpanNear shouldBe true
        val near = q.spanNear()
        near.clauses().size shouldBe 2
        near.clauses()[0].isSpanTerm shouldBe true
        near.clauses()[1].isSpanNear shouldBe true
    }
})
