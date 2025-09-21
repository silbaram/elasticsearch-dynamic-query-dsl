package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PercolateQueryTest : FunSpec({

    test("최상위 percolate 쿼리 - 단일 문서") {
        val q = query {
            percolateQuery {
                field = "queries" // percolator type field
                document = mapOf(
                    "message" to "Elasticsearch percolator test",
                    "tags" to listOf("search", "kotlin")
                )
                boost = 1.2f
                _name = "perco_single"
            }
        }

        q.isPercolate shouldBe true
        val p = q.percolate()
        p.field() shouldBe "queries"
        p.boost() shouldBe 1.2f
        p.queryName() shouldBe "perco_single"
        // 직렬화 형태는 클라이언트 버전에 따라 다를 수 있으므로 간단 확인만 수행
        q.toString().isNotBlank() shouldBe true
    }

    test("percolate - 다중 문서(documents) + bool.must 내 사용") {
        val q = query {
            boolQuery {
                mustQuery {
                    percolateQuery {
                        field = "queries"
                        documents = listOf(
                            mapOf("message" to "first"),
                            mapOf("message" to "second", "user" to "kimchy")
                        )
                    }
                }
            }
        }

        q.isBool shouldBe true
        val must = q.bool().must()
        must.size shouldBe 1
        val p = must.first().percolate()
        p.field() shouldBe "queries"
        // 직렬화 형태는 클라이언트 버전에 따라 다를 수 있으므로 간단 확인만 수행
        must.first().toString().isNotBlank() shouldBe true
    }

    test("percolate - 기존 문서 참조(index/id/routing)") {
        val q = query {
            percolateQuery {
                field = "queries"
                index = "docs"
                id = "_doc_1"
                routing = "user-1"
            }
        }

        q.isPercolate shouldBe true
        val p = q.percolate()
        p.field() shouldBe "queries"
        p.index() shouldBe "docs"
        p.id() shouldBe "_doc_1"
        p.routing() shouldBe "user-1"
    }

    test("유효하지 않은 입력은 생략되어야 함") {
        // 빈 field
        val q1 = queryOrNull { percolateQuery { field = " "; document = mapOf("a" to 1) } }
        q1 shouldBe null

        // field는 있지만 문서/참조 없음
        val q2 = queryOrNull { percolateQuery { field = "queries" } }
        q2 shouldBe null

        // 빈 map만 전달되는 경우
        val q3 = queryOrNull { percolateQuery { field = "queries"; document = emptyMap() } }
        q3 shouldBe null
    }
})
