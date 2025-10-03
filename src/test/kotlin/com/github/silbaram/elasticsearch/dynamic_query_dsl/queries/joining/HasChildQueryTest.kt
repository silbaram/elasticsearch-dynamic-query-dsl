package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.joining

import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class HasChildQueryTest : FunSpec({

    test("has_child 쿼리가 자식 조건과 함께 생성되어야 함") {
        val result = query {
            boolQuery {
                mustQuery {
                    hasChildQuery {
                        type = "reply"
                        minChildren = 1
                        maxChildren = 5
                        scoreMode = ChildScoreMode.Sum
                        ignoreUnmapped = true
                        query {
                            matchQuery {
                                field = "reply.text"
                                query = "ok"
                            }
                        }
                        innerHits {
                            name("replies")
                            size(3)
                        }
                    }
                }
            }
        }

        val must = result.bool().must()
        must.size shouldBe 1
        val hasChild = must.first()
        hasChild.isHasChild shouldBe true
        val hc = hasChild.hasChild()
        hc.type() shouldBe "reply"
        hc.minChildren() shouldBe 1
        hc.maxChildren() shouldBe 5
        hc.scoreMode() shouldBe ChildScoreMode.Sum
        hc.ignoreUnmapped() shouldBe true
        hc.innerHits()?.name() shouldBe "replies"
        hc.innerHits()?.size() shouldBe 3
        hc.query().isMatch shouldBe true
    }

    test("type 또는 query가 없으면 has_child 쿼리가 생성되지 않아야 함") {
        queryOrNull {
            hasChildQuery {
                type = "reply"
            }
        }.shouldBeNull()

        queryOrNull {
            hasChildQuery {
                query {
                    matchQuery {
                        field = "reply.text"
                        query = "ok"
                    }
                }
            }
        }.shouldBeNull()
    }
})
