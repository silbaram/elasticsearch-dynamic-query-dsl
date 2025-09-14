package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.rangeQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SpanMultiQueryTest : FunSpec({

    test("span_multi: range 쿼리를 match로 래핑") {
        val match = rangeQuery(
            field = "publish_date",
            gte = "2023-01-01"
        )

        val q = spanMultiQuery(
            match = match,
            boost = 1.1f,
            _name = "range-as-span"
        )

        q shouldNotBe null
        q!!.isSpanMulti shouldBe true

        val sm = q.spanMulti()
        sm.boost() shouldBe 1.1f
        sm.queryName() shouldBe "range-as-span"
        sm.match().isRange shouldBe true
        sm.match().range().field() shouldBe "publish_date"
        sm.match().range().gte()?.to(String::class.java) shouldBe "2023-01-01"
    }

    test("span_multi: span_near 내부 clause로 사용") {
        val near = query {
            spanNearQuery {
                clauses[
                    spanTermQuery("title", "elasticsearch"),
                    spanMultiQuery(
                        match = rangeQuery("publish_date", gte = "2023-01-01")
                    )
                ]
                slop = 3
                inOrder = true
            }
        }

        near.isSpanNear shouldBe true
        val built = near.spanNear()
        built.clauses().size shouldBe 2
        built.clauses()[0].isSpanTerm shouldBe true
        built.clauses()[1].isSpanMulti shouldBe true
        built.clauses()[1].spanMulti().match().isRange shouldBe true
    }

    test("query { spanMultiQuery { ... } }: DSL 형태와 생략 규칙") {
        // 정상 DSL
        val q = query {
            spanMultiQuery {
                match { rangeQuery("publish_date", gte = "2023-01-01") }
                boost = 2.0f
                _name = "dsl-range-span"
            }
        }

        q.isSpanMulti shouldBe true
        q.spanMulti().boost() shouldBe 2.0f
        q.spanMulti().queryName() shouldBe "dsl-range-span"
        q.spanMulti().match().isRange shouldBe true

        // match 미제공 → no-op (fallback 사용)
        val q2 = query {
            spanMultiQuery {
                // no match
                boost = 1.0f
            }
            matchAll { it } // fallback
        }
        q2.isMatchAll shouldBe true

        // 멀티텀이 아닌 match 사용 → no-op (fallback 사용)
        val q3 = query {
            spanMultiQuery {
                match { spanTermQuery("title", "x") }
            }
            matchAll { it } // fallback
        }
        q3.isMatchAll shouldBe true
    }

    test("spanMultiQuery(match = null) 또는 비-멀티텀은 null 반환") {
        val nullMatch = spanMultiQuery(match = null)
        nullMatch shouldBe null

        val nonMulti = spanMultiQuery(match = spanTermQuery("f", "v"))
        nonMulti shouldBe null
    }
})
