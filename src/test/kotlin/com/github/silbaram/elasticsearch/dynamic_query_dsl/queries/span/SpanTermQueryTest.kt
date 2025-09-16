package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SpanTermQueryTest : FunSpec({

    test("span_term: DSL 루트(query { ... }) 기본 동작 및 속성 확인") {
        val q = query {
            spanTermQuery {
                field = "title"
                value = "kotlin"
                boost = 1.2f
                _name = "term_kotlin"
            }
        }

        q.isSpanTerm shouldBe true
        val st = q.spanTerm()
        st.field() shouldBe "title"
        // Value extraction API differs across client variants; assert content presence
        st.value().toString().lowercase().contains("kotlin") shouldBe true
        st.boost() shouldBe 1.2f
        st.queryName() shouldBe "term_kotlin"
    }

    test("span_term: DSL no-op 시 fallback 유지 (invalid helper ignored)") {
        val built = query {
            // 블록 DSL에서 유효하지 않은 입력 → no-op
            spanTermQuery {
                field = "title"
                value = " "
            }
            // fallback
            matchAll { it }
        }

        built.isMatchAll shouldBe true
    }
})
