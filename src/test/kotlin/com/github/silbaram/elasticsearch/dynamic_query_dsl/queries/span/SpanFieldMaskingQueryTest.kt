package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.matchAllQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SpanFieldMaskingQueryTest : FunSpec({

    test("span_field_masking: 기본 동작 및 속성 확인") {
        val q =
            query {
                spanFieldMaskingQuery {
                    query {
                        spanTermQuery("text.stems", "fox")
                    }
                    field = "text"
                    boost = 2.0f
                    _name = "mask-fox"
                }
            }

        q shouldNotBe null
        // top-level kind check
        q.isSpanFieldMasking shouldBe true

        val sfm = q.spanFieldMasking()
        sfm.field() shouldBe "text"
        sfm.boost() shouldBe 2.0f
        sfm.queryName() shouldBe "mask-fox"

        // inner span query retained
        sfm.query().isSpanTerm shouldBe true
    }

    test("span_field_masking: null/blank 입력 시 생략") {
        // null value로 인해 spanTermQuery가 null을 반환하므로 spanFieldMaskingQuery도 no-op
        val nullInnerResult =
            query {
                spanFieldMaskingQuery {
                    query {
                        spanTermQuery("text.stems", null)
                    }
                    field = "text"
                }
                matchAll { it } // fallback
            }
        nullInnerResult.isMatchAll shouldBe true // spanFieldMaskingQuery가 no-op이므로 matchAll만 남음

        // blank field로 인해 spanFieldMaskingQuery가 no-op
        val blankFieldResult =
            query {
                spanFieldMaskingQuery {
                    query {
                        spanTermQuery("text.stems", "fox")
                    }
                    field = " " // blank field
                }
                matchAll { it } // fallback
            }
        blankFieldResult.isMatchAll shouldBe true // spanFieldMaskingQuery가 no-op이므로 matchAll만 남음

        // 빈 query 블록으로 인해 spanFieldMaskingQuery가 no-op
        val emptyQueryResult =
            query {
                spanFieldMaskingQuery {
                    // query 블록을 설정하지 않음
                    field = "text"
                }
                matchAll { it } // fallback
            }
        emptyQueryResult.isMatchAll shouldBe true // spanFieldMaskingQuery가 no-op이므로 matchAll만 남음
    }

    test("span_field_masking: span_near 내에서 동작") {
        val near =
            query {
                spanNearQuery {
                    // Array-style DSL - clauses[query1, query2, ...] 형태로 사용
                    clauses[
                        spanTermQuery("text", "quick"),
                        spanFieldMaskingQuery(
                            query = spanTermQuery("text.stems", "fox"),
                            field = "text"
                        )
                    ]
                    slop = 5
                    inOrder = false
                }
            }

        near shouldNotBe null
        near.isSpanNear shouldBe true

        val built = near.spanNear()
        built.clauses().size shouldBe 2
        built.clauses()[0].isSpanTerm shouldBe true
        built.clauses()[1].isSpanFieldMasking shouldBe true

        val sfm = built.clauses()[1].spanFieldMasking()
        sfm.field() shouldBe "text"
        sfm.query().isSpanTerm shouldBe true
    }

    test("query { spanFieldMaskingQuery { ... } }: DSL 형태로 사용") {
        val q = query {
            spanFieldMaskingQuery {
                query { spanTermQuery("text.stems", "fox") }
                field = "text"
                boost = 2.0f
                _name = "mask-query"
            }
        }

        q shouldNotBe null
        q.isSpanFieldMasking shouldBe true

        val sfm = q.spanFieldMasking()
        sfm.field() shouldBe "text"
        sfm.boost() shouldBe 2.0f
        sfm.queryName() shouldBe "mask-query"
        sfm.query().isSpanTerm shouldBe true
    }

    test("query { spanFieldMaskingQuery { ... } }: 빈 쿼리 처리") {
        // DSL에서 빈 쿼리 시 no-op 되는지 확인
        val q = query {
            spanFieldMaskingQuery {
                // query를 설정하지 않음 (빈 쿼리)
                field = "text"
            }
            matchAll { it } // fallback으로 추가
        }

        // spanFieldMaskingQuery가 no-op이므로 match_all만 남음
        q.isMatchAll shouldBe true

        // DSL에서 null field 시 no-op 되는지 확인
        val q2 = query {
            spanFieldMaskingQuery {
                query { spanTermQuery("text.stems", "fox") }
                field = null // null field
            }
            matchAll { it } // fallback으로 추가
        }

        // spanFieldMaskingQuery가 no-op이므로 match_all만 남음
        q2.isMatchAll shouldBe true
    }

    test("SubQueryBuilders와의 통합: query 함수 내에서 사용") {
        val spanMaskingQuery =
            query {
                spanFieldMaskingQuery {
                    query {
                        spanTermQuery("text.stems", "fox")
                    }
                    field = "text"
                }
            }
        
        spanMaskingQuery shouldNotBe null
        spanMaskingQuery.isSpanFieldMasking shouldBe true
        
        val sfm = spanMaskingQuery.spanFieldMasking()
        sfm.field() shouldBe "text"
        sfm.query().isSpanTerm shouldBe true
    }
})
