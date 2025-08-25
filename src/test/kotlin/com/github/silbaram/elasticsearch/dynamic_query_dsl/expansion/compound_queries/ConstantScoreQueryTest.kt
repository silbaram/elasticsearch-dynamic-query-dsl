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

    test("최상위 constant_score 쿼리가 올바르게 생성되어야 한다") {
        val q = query {
            constantScoreQuery {
                filterQuery {
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

    test("constant_score는 must, filter, mustNot 절에서 사용될 수 있다") {
        val q = query {
            boolQuery {
                mustQuery {
                    constantScoreQuery {
                        filterQuery { termQuery(field = "brand", value = "Samsung") }
                    }
                }
                filterQuery {
                    constantScoreQuery {
                        filterQuery { termQuery(field = "category", value = "Electronics") }
                    }
                }
                mustNotQuery {
                    constantScoreQuery {
                        filterQuery { termQuery(field = "status", value = "inactive") }
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

    test("필터가 비어있을 때 최상위 constant_score는 건너뛰어야 한다") {
        shouldThrow<Exception> {
            query {
                constantScoreQuery {
                    filterQuery {
                        termQuery(field = "brand", value = null)
                    }
                }
            }
        }
    }

    test("bool 절 내부의 constant_score는 필터가 비어있을 때 건너뛰어야 한다") {
        val q = query {
            boolQuery {
                mustQuery {
                    constantScoreQuery {
                        filterQuery {
                            termQuery(field = "brand", value = null)
                        }
                    }
                }
            }
        }

        q.bool().must().size shouldBe 0
    }

    test("queries를 사용하는 필터 블록은 bool 쿼리로 래핑되어야 한다") {
        val q = query {
            boolQuery {
                filterQuery {
                    constantScoreQuery {
                        filterQuery {
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

    test("constant_score 필터는 복잡한 bool 쿼리를 포함할 수 있다") {
        val q = query {
            constantScoreQuery {
                filterQuery {
                    boolQuery {
                        mustQuery {
                            termQuery("field1", "value1")
                        }
                        shouldQuery {
                            queries[
                                termQuery("field2", "value2"),
                                termQuery("field3", "value3")
                            ]
                        }.minimumShouldMatch("1")
                    }
                }
                boost = 2.0f
            }
        }

        q.isConstantScore shouldBe true
        val cs = q.constantScore()
        cs.boost() shouldBe 2.0f

        val filter = cs.filter()
        filter.isBool shouldBe true

        val bool = filter.bool()
        bool.must().size shouldBe 1
        bool.must().first().term().field() shouldBe "field1"
        bool.should().size shouldBe 2
        bool.minimumShouldMatch() shouldBe "1"
    }

    test("should 절의 constant_score는 점수를 높인다") {
        val q = query {
            boolQuery {
                mustQuery {
                    termQuery("content", "search")
                }
                shouldQuery {
                    constantScoreQuery {
                        filterQuery {
                            termQuery("featured", "text")
                        }
                        boost = 1.5f
                    }
                }
            }
        }

        q.isBool shouldBe true
        val bool = q.bool()
        bool.must().size shouldBe 1
        bool.must().first().term().field() shouldBe "content"
        bool.should().size shouldBe 1

        val cs = bool.should().first().constantScore()
        cs.filter().term().field() shouldBe "featured"
        cs.filter().term().value().stringValue() shouldBe "text"
        cs.boost() shouldBe 1.5f
    }
})
