package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.joining

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class HasParentQueryTest : FunSpec({

    test("has_parent 쿼리가 parentType과 score 옵션을 반영해야 함") {
        val result = query {
            boolQuery {
                mustQuery {
                    queries[
                        { hasParentQuery {
                            parentType = "question"
                            score = true
                            ignoreUnmapped = false
                            query {
                                matchQuery {
                                    field = "question.title"
                                    query = "search"
                                }
                            }
                            innerHits {
                                name("questions")
                                size(1)
                            }
                        } }
                    ]
                }
            }
        }

        val must = result.bool().must()
        must.size shouldBe 1
        val hasParent = must.first()
        hasParent.isHasParent shouldBe true
        val hp = hasParent.hasParent()
        hp.parentType() shouldBe "question"
        hp.score() shouldBe true
        hp.ignoreUnmapped() shouldBe false
        hp.innerHits()?.name() shouldBe "questions"
        hp.innerHits()?.size() shouldBe 1
        hp.query().isMatch shouldBe true
    }

    test("parentType 또는 query가 없으면 has_parent 쿼리가 생성되지 않아야 함") {
        queryOrNull {
            hasParentQuery {
                parentType = "question"
            }
        }.shouldBeNull()

        queryOrNull {
            hasParentQuery {
                query {
                    matchQuery {
                        field = "question.title"
                        query = "search"
                    }
                }
            }
        }.shouldBeNull()
    }
})
