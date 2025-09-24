package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.joining

import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class NestedQueryTest : FunSpec({

    test("nested 쿼리가 path, scoreMode, innerHits와 함께 생성되어야 함") {
        val result = query {
            nestedQuery {
                path = "comments"
                ignoreUnmapped = true
                scoreMode = ChildScoreMode.Avg
                boost = 1.2f
                _name = "nested-comments"
                query {
                    matchQuery {
                        field = "comments.text"
                        query = "elastic"
                    }
                }
                innerHits {
                    name("popular")
                    size(2)
                }
            }
        }

        result.isNested shouldBe true
        val nested = result.nested()
        nested.path() shouldBe "comments"
        nested.ignoreUnmapped() shouldBe true
        nested.scoreMode() shouldBe ChildScoreMode.Avg
        nested.query().isMatch shouldBe true
        nested.innerHits()?.name() shouldBe "popular"
        nested.innerHits()?.size() shouldBe 2
        nested.boost() shouldBe 1.2f
        nested.queryName() shouldBe "nested-comments"
    }

    test("path 또는 query가 없으면 nested 쿼리를 생성하지 않아야 함") {
        queryOrNull {
            nestedQuery {
                path = "comments"
            }
        }.shouldBeNull()

        queryOrNull {
            nestedQuery {
                query {
                    matchQuery {
                        field = "comments.text"
                        query = "elastic"
                    }
                }
            }
        }.shouldBeNull()
    }
})
