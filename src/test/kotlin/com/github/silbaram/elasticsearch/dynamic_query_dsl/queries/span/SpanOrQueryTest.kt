package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SpanOrQueryTest : FunSpec({

    test("span_or: 기본 동작 및 속성 확인") {
        val q = query {
            spanOrQuery {
                clauses[
                    query { spanTermQuery { field = "title"; value = "kotlin" } },
                    query { spanTermQuery { field = "title"; value = "dsl" } }
                ]
                boost = 1.1f
                _name = "span_or_example"
            }
        }

        q shouldNotBe null
        q.isSpanOr shouldBe true
        val so = q.spanOr()
        so.boost() shouldBe 1.1f
        so.queryName() shouldBe "span_or_example"
        so.clauses().size shouldBe 2
        so.clauses()[0].isSpanTerm shouldBe true
    }

    test("span_or: 비-span 혼합 시 필터링, 모두 비-span이면 null") {
        val q = query {
            spanOrQuery {
                clauses[
                    com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query { matchQuery { field = "f"; query = "v" } },
                    query { spanTermQuery { field = "f"; value = "v" } }
                ]
            }
        }
        q shouldNotBe null
        q.spanOr().clauses().size shouldBe 1
        q.spanOr().clauses()[0].isSpanTerm shouldBe true

        val q2 = queryOrNull {
            spanOrQuery { clauses[ com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query { matchQuery { field = "f"; query = "v" } } ] }
        }
        q2 shouldBe null
    }

    test("span_or: 블록 DSL 동작 및 no-op fallback") {
        val built = query {
            spanOrQuery {
                clauses[
                    query { spanTermQuery { field = "title"; value = "kotlin" } },
                    com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query { matchQuery { field = "title"; query = "dsl" } } // 비-span은 필터링되어 제외됨
                ]
                _name = "span_or_dsl"
            }
        }
        built.isSpanOr shouldBe true
        built.spanOr().queryName() shouldBe "span_or_dsl"
        built.spanOr().clauses().size shouldBe 1

        val fallback = query {
            spanOrQuery {
                // 유효 clause 없음 → no-op
                clauses[ com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query { matchQuery { field = "f"; query = "v" } } ]
            }
            matchAll { it }
        }
        fallback.isMatchAll shouldBe true
    }
})
