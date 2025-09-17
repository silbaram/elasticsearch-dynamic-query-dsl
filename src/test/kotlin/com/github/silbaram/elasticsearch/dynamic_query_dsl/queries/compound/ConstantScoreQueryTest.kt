package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.integrations.constantScoreQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.constantScoreQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConstantScoreQueryTest : FunSpec({

    test("최상위 constant_score 쿼리가 올바르게 생성되어야 한다") {
        val query = query {
            constantScoreQuery {
                filterQuery {
                    termQuery { field = "brand"; value = "Samsung" }
                }
                boost = 10.0f
                _name = "named"
            }
        }

        query.isConstantScore shouldBe true
        query.constantScore().filter().isTerm shouldBe true
        query.constantScore().filter().term().field() shouldBe "brand"
        query.constantScore().filter().term().value().stringValue() shouldBe "Samsung"
        query.constantScore().boost() shouldBe 10.0f
        query.constantScore().queryName() shouldBe "named"
    }

    test("constant_score는 must, filter, mustNot 절에서 사용될 수 있다") {
        val query = query {
            boolQuery {
                mustQuery {
                    constantScoreQuery {
                        filterQuery { termQuery { field = "brand"; value = "Samsung" } }
                    }
                }
                filterQuery {
                    constantScoreQuery {
                        filterQuery { termQuery { field = "category"; value = "Electronics" } }
                    }
                }
                mustNotQuery {
                    constantScoreQuery {
                        filterQuery { termQuery { field = "status"; value = "inactive" } }
                    }
                }
            }
        }

        query.bool().must().size shouldBe 1
        query.bool().filter().size shouldBe 1
        query.bool().mustNot().size shouldBe 1
        query.bool().must().first().constantScore().filter().term().field() shouldBe "brand"
        query.bool().filter().first().constantScore().filter().term().field() shouldBe "category"
        query.bool().mustNot().first().constantScore().filter().term().field() shouldBe "status"
    }

    test("필터가 비어있을 때 최상위 constant_score는 건너뛰어야 한다") {
        val q = queryOrNull {
            constantScoreQuery {
                filterQuery {
                    termQuery { field = "brand"; value = null }
                }
            }
        }
        q shouldBe null
    }

    test("bool 절 내부의 constant_score는 필터가 비어있을 때 건너뛰어야 한다") {
        val query = query {
            boolQuery {
                mustQuery {
                    constantScoreQuery {
                        filterQuery {
                            termQuery { field = "brand"; value = null }
                        }
                    }
                }
            }
        }

        query.bool().must().size shouldBe 0
    }

    test("queries를 사용하는 필터 블록은 bool 쿼리로 래핑되어야 한다") {
        val query = query {
            boolQuery {
                filterQuery {
                    constantScoreQuery {
                        filterQuery {
                            queries[
                                { termQuery { field = "brand"; value = "Samsung" } },
                                { termQuery { field = "category"; value = "Electronics" } }
                            ]
                        }
                    }
                }
            }
        }

        val csFilter = query.bool().filter().first().constantScore().filter()
        csFilter.isBool shouldBe true
        csFilter.bool().filter().size shouldBe 2
        csFilter.bool().filter().any { it.term().field() == "brand" } shouldBe true
        csFilter.bool().filter().any { it.term().field() == "category" } shouldBe true
    }

    test("constant_score 필터는 복잡한 bool 쿼리를 포함할 수 있다") {
        val query = query {
            constantScoreQuery {
                filterQuery {
                    boolQuery {
                        mustQuery {
                                termQuery { field = "field1"; value = "value1" }
                        }
                        shouldQuery {
                            queries[
                                { termQuery { field = "field2"; value = "value2" } },
                                { termQuery { field = "field3"; value = "value3" } }
                            ]
                        }.minimumShouldMatch("1")
                    }
                }
                boost = 2.0f
            }
        }

        query.isConstantScore shouldBe true
        val cs = query.constantScore()
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
        val query = query {
            boolQuery {
                mustQuery {
                    termQuery { field = "content"; value = "search" }
                }
                shouldQuery {
                    constantScoreQuery {
                        filterQuery {
                            termQuery { field = "featured"; value = "text" }
                        }
                        boost = 1.5f
                    }
                }
            }
        }

        query.isBool shouldBe true
        val bool = query.bool()
        bool.must().size shouldBe 1
        bool.must().first().term().field() shouldBe "content"
        bool.should().size shouldBe 1

        val cs = bool.should().first().constantScore()
        cs.filter().term().field() shouldBe "featured"
        cs.filter().term().value().stringValue() shouldBe "text"
        cs.boost() shouldBe 1.5f
    }
})
