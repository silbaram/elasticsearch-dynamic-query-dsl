package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SpanFirstDslTest : FunSpec({
    test("span_first: 블록 DSL 동작 및 속성 확인") {
        val q = query {
            spanFirstQueryDsl {
                match { spanTermQuery("user.id", "kimchy") }
                end = 3
                boost = 1.2f
                _name = "first_dsl"
            }
        }

        q.isSpanFirst shouldBe true
        val sf = q.spanFirst()
        sf.end() shouldBe 3
        sf.boost() shouldBe 1.2f
        sf.queryName() shouldBe "first_dsl"
        sf.match().isSpanTerm shouldBe true
    }
})
