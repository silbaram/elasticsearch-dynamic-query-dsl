package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class PinnedQueryTest : FunSpec({

    test("pinned query - builds with ids and organic query") {
        val q = query {
            pinnedQuery {
                ids("1", "2 ", null, " 3")
                organic {
                    matchQuery {
                        field = "title"
                        query = "elasticsearch"
                    }
                }
                boost = 1.1f
                _name = "pinned-sample"
            }
        }

        q.shouldNotBeNull()
        q.isPinned shouldBe true
        val pinned = q.pinned()
        pinned.boost() shouldBe 1.1f
        pinned.queryName() shouldBe "pinned-sample"
        pinned.ids() shouldContainExactly listOf("1", "2", "3")
        pinned.organic().isMatch shouldBe true
        pinned.organic().match().field() shouldBe "title"
    }

    test("pinned query - usable inside bool.must helper") {
        val q = query {
            boolQuery {
                mustQuery {
                    pinnedQuery {
                        ids = listOf("a", "b")
                        organic {
                            matchQuery {
                                field = "status"
                                query = "active"
                            }
                        }
                    }
                }
            }
        }

        q.isBool shouldBe true
        val mustQueries = q.bool().must()
        mustQueries.size shouldBe 1
        val nested = mustQueries.first()
        nested.isPinned shouldBe true
        nested.pinned().ids() shouldContainExactly listOf("a", "b")
    }

    test("pinned query - invalid definitions are skipped") {
        val noIds = queryOrNull {
            pinnedQuery {
                organic {
                    matchQuery {
                        field = "title"
                        query = "search"
                    }
                }
            }
        }
        noIds shouldBe null

        val noOrganic = queryOrNull {
            pinnedQuery {
                ids("1", "2")
            }
        }
        noOrganic shouldBe null
    }
})
