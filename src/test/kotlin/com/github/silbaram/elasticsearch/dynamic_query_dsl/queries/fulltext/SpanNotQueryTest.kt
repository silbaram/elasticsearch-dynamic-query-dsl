package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SpanNotQueryTest : FunSpec({

    test("span_not: 기본 동작 및 속성 확인") {
        val include = spanNearQuery(
            clauses = listOf(
                spanTermQuery("title", "kotlin"),
                spanTermQuery("title", "dsl")
            ),
            slop = 3,
            inOrder = true
        )
        val exclude = spanTermQuery("title", "legacy")

        val q = spanNotQuery(
            include = include,
            exclude = exclude,
            pre = 0,
            post = 1,
            boost = 1.0f,
            _name = "not_legacy"
        )

        q shouldNotBe null
        q!!.isSpanNot shouldBe true
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
                exclude { spanTermQuery("f", "v") }
                pre = -1 // 무시
            }
            matchAll { it } // fallback 유지
        }

        q.isMatchAll shouldBe true
    }

    test("span_not: include 또는 exclude 가 null이면 null 반환") {
        val ex = spanTermQuery("f", "v")
        val q1 = spanNotQuery(include = null, exclude = ex)
        q1 shouldBe null

        val inc = spanTermQuery("f", "v")
        val q2 = spanNotQuery(include = inc, exclude = null)
        q2 shouldBe null
    }

    test("span_not: include/exclude 에 비-span 전달 시 생략") {
        val nonSpan = matchQuery("title", "text")
        val ex = spanTermQuery("title", "legacy")
        val q = spanNotQuery(include = nonSpan, exclude = ex)
        q shouldBe null
    }

    test("span_not: 중첩 span_not 허용") {
        val inner = spanNotQuery(
            include = spanTermQuery("body", "green"),
            exclude = spanTermQuery("body", "apple")
        )

        val outer = spanNotQuery(
            include = spanTermQuery("body", "fruit"),
            exclude = inner
        )

        outer shouldNotBe null
        outer!!.isSpanNot shouldBe true
        val sn = outer.spanNot()
        sn.include().isSpanTerm shouldBe true
        sn.exclude().isSpanNot shouldBe true
    }
})

