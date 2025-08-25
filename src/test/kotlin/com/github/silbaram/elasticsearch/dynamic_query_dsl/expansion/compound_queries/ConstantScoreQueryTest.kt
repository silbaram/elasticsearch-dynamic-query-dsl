package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level.constantScoreQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.query
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConstantScoreQueryTest : FunSpec({

    test("top-level constant_score query should be built correctly") {
        val q = query {
            constantScoreQuery {
                filter {
                    termQuery(field = "brand", value = "Samsung")
                }
                boost = 10.0f
                _name = "named"
            }
        }

        q.isConstantScore shouldBe true
        q.constantScore().filter().isTerm shouldBe true
        q.constantScore().filter().term().field() shouldBe "brand"
        q.constantScore().filter().term().value().stringValue() shouldBe "Samsung"
        q.constantScore().boost() shouldBe 10.0f
        q.constantScore().queryName() shouldBe "named"
    }

    test("should clause should contain both term and constant_score queries") {
        val boolQuery = query {
            boolQuery {
                shouldQuery {
                    +termQuery(field = "description", value = "노트북")
                    constantScoreQuery {
                        filter {
                            termQuery(field = "brand", value = "Samsung")
                        }
                        boost = 5.0f
                        _name = "cs-boost"
                    }
                }
            }
        }

        val shouldClauses = boolQuery.bool().should()
        shouldClauses.size shouldBe 2
        shouldClauses.any { it.isTerm && it.term().field() == "description" } shouldBe true
        shouldClauses.any { it.isConstantScore } shouldBe true

        val cs = shouldClauses.first { it.isConstantScore }.constantScore()
        cs.boost() shouldBe 5.0f
        cs.queryName() shouldBe "cs-boost"
    }

    test("constant_score can be used in must, filter and mustNot clauses") {
        val q = query {
            boolQuery {
                mustQuery {
                    constantScoreQuery {
                        filter { termQuery(field = "brand", value = "Samsung") }
                    }
                }
                filterQuery {
                    constantScoreQuery {
                        filter { termQuery(field = "category", value = "Electronics") }
                    }
                }
                mustNotQuery {
                    constantScoreQuery {
                        filter { termQuery(field = "status", value = "inactive") }
                    }
                }
            }
        }

        q.bool().must().size shouldBe 1
        q.bool().filter().size shouldBe 1
        q.bool().mustNot().size shouldBe 1
        q.bool().must().first().constantScore().filter().term().field() shouldBe "brand"
        q.bool().filter().first().constantScore().filter().term().field() shouldBe "category"
        q.bool().mustNot().first().constantScore().filter().term().field() shouldBe "status"
    }

    test("top-level constant_score should be skipped when filter is empty") {
        shouldThrow<Exception> {
            query {
                constantScoreQuery {
                    filter {
                        termQuery(field = "brand", value = null)
                    }
                }
            }
        }
    }

    test("constant_score inside bool clause should be skipped when filter is empty") {
        val q = query {
            boolQuery {
                mustQuery {
                    constantScoreQuery {
                        filter {
                            termQuery(field = "brand", value = null)
                        }
                    }
                }
            }
        }

        q.bool().must().size shouldBe 0
    }

    test("filter block using queries should be wrapped with bool query") {
        val q = query {
            boolQuery {
                filterQuery {
                    constantScoreQuery {
                        filter {
                            queries[
                                termQuery(field = "brand", value = "Samsung"),
                                termQuery(field = "category", value = "Electronics")
                            ]
                        }
                    }
                }
            }
        }

        val csFilter = q.bool().filter().first().constantScore().filter()
        csFilter.isBool shouldBe true
        csFilter.bool().must().size shouldBe 2
        csFilter.bool().must().any { it.term().field() == "brand" } shouldBe true
        csFilter.bool().must().any { it.term().field() == "category" } shouldBe true
    }
})
