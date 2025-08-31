package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MatchQueryTest : FunSpec({

    test("must 쿼리에서 match 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        matchQuery(
                            field = "a",
                            query = "1111"
                        ),
                        matchQuery(
                            field = "b",
                            query = "2222"
                        )
                    ]
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 2
        mustQuery.filter { it.isMatch }.find { it.match().field() == "a" }?.match()?.query()?.stringValue() shouldBe "1111"
        mustQuery.filter { it.isMatch }.find { it.match().field() == "b" }?.match()?.query()?.stringValue() shouldBe "2222"
    }

    test("must 쿼리에서 matchQuery에 query 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        matchQuery(
                            field = "a",
                            query = null
                        ),
                        matchQuery(
                            field = "b",
                            query = ""
                        ),
                        matchQuery(
                            field = "c",
                            query = "3333"
                        )
                    ]
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isMatch }.find { it.match().field() == "a" } shouldBe null
        mustQuery.filter { it.isMatch }.find { it.match().field() == "b" } shouldBe null
        mustQuery.filter { it.isMatch }.find { it.match().field() == "c" }?.match()?.query()?.stringValue() shouldBe "3333"
    }

    test("must 쿼리에서 matchQuery가 없을때 must쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        matchQuery(
                            field = "a",
                            query = ""
                        ),
                        matchQuery(
                            field = "b",
                            query = null
                        )
                    ]
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 0
    }

    test("filter 쿼리에서 match 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                filterQuery {
                    queries[
                        matchQuery(
                            field = "a",
                            query = "1111"
                        ),
                        matchQuery(
                            field = "b",
                            query = "2222"
                        )
                    ]
                }
            }
        }

        val filterQuery = query.bool().filter()

        query.isBool shouldBe true
        filterQuery.size shouldBe 2
        filterQuery.filter { it.isMatch }.find { it.match().field() == "a" }?.match()?.query()?.stringValue() shouldBe "1111"
        filterQuery.filter { it.isMatch }.find { it.match().field() == "b" }?.match()?.query()?.stringValue() shouldBe "2222"
    }

    test("filter 쿼리에서 matchQuery에 query 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                filterQuery {
                    queries[
                        matchQuery(
                            field = "a",
                            query = null
                        ),
                        matchQuery(
                            field = "b",
                            query = ""
                        ),
                        matchQuery(
                            field = "c",
                            query = "3333"
                        )
                    ]
                }
            }
        }

        val filterQuery = query.bool().filter()

        query.isBool shouldBe true
        filterQuery.size shouldBe 1
        filterQuery.filter { it.isMatch }.find { it.match().field() == "a" } shouldBe null
        filterQuery.filter { it.isMatch }.find { it.match().field() == "b" } shouldBe null
        filterQuery.filter { it.isMatch }.find { it.match().field() == "c" }?.match()?.query()?.stringValue() shouldBe "3333"
    }

    test("filter 쿼리에서 matchQuery가 없을때 filter쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                filterQuery {
                    queries[
                        matchQuery(
                            field = "a",
                            query = ""
                        ),
                        matchQuery(
                            field = "b",
                            query = null
                        )
                    ]
                }
            }
        }


        val filterQuery = query.bool().filter()

        query.isBool shouldBe true
        filterQuery.size shouldBe 0
    }

    test("mustNot 쿼리에서 match 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                mustNotQuery {
                    queries[
                        matchQuery(
                            field = "a",
                            query = "1111"
                        ),
                        matchQuery(
                            field = "b",
                            query = "2222"
                        )
                    ]
                }
            }
        }

        val mustNotQuery = query.bool().mustNot()

        query.isBool shouldBe true
        mustNotQuery.size shouldBe 2
        mustNotQuery.filter { it.isMatch }.find { it.match().field() == "a" }?.match()?.query()?.stringValue() shouldBe "1111"
        mustNotQuery.filter { it.isMatch }.find { it.match().field() == "b" }?.match()?.query()?.stringValue() shouldBe "2222"
    }

    test("mustNot 쿼리에서 matchQuery에 query 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                mustNotQuery {
                    queries[
                        matchQuery(
                            field = "a",
                            query = null
                        ),
                        matchQuery(
                            field = "b",
                            query = ""
                        ),
                        matchQuery(
                            field = "c",
                            query = "3333"
                        )
                    ]
                }
            }
        }

        val mustNotQuery = query.bool().mustNot()

        query.isBool shouldBe true
        mustNotQuery.size shouldBe 1
        mustNotQuery.filter { it.isMatch }.find { it.match().field() == "a" } shouldBe null
        mustNotQuery.filter { it.isMatch }.find { it.match().field() == "b" } shouldBe null
        mustNotQuery.filter { it.isMatch }.find { it.match().field() == "c" }?.match()?.query()?.stringValue() shouldBe "3333"
    }

    test("mustNot 쿼리에서 matchQuery가 없을때 mustNot쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                mustNotQuery {
                    queries[
                        matchQuery(
                            field = "a",
                            query = ""
                        ),
                        matchQuery(
                            field = "b",
                            query = null
                        )
                    ]
                }
            }
        }
        val mustNotQuery = query.bool().mustNot()

        query.isBool shouldBe true
        mustNotQuery.size shouldBe 0
    }

    test("should 쿼리에서 match 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                shouldQuery {
                    queries[
                        matchQuery(
                            field = "a",
                            query = "1111"
                        ),
                        matchQuery(
                            field = "b",
                            query = "2222"
                        )
                    ]
                }
            }
        }

        val shouldQuery = query.bool().should()

        query.isBool shouldBe true
        shouldQuery.size shouldBe 2
        shouldQuery.filter { it.isMatch }.find { it.match().field() == "a" }?.match()?.query()?.stringValue() shouldBe "1111"
        shouldQuery.filter { it.isMatch }.find { it.match().field() == "b" }?.match()?.query()?.stringValue() shouldBe "2222"
    }

    test("should 쿼리에서 matchQuery에 query 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                shouldQuery {
                    queries[
                        matchQuery(
                            field = "a",
                            query = null
                        ),
                        matchQuery(
                            field = "b",
                            query = ""
                        ),
                        matchQuery(
                            field = "c",
                            query = "3333"
                        )
                    ]
                }
            }
        }

        val shouldQuery = query.bool().should()

        query.isBool shouldBe true
        shouldQuery.size shouldBe 1
        shouldQuery.filter { it.isMatch }.find { it.match().field() == "a" } shouldBe null
        shouldQuery.filter { it.isMatch }.find { it.match().field() == "b" } shouldBe null
        shouldQuery.filter { it.isMatch }.find { it.match().field() == "c" }?.match()?.query()?.stringValue() shouldBe "3333"
    }

    test("should 쿼리에서 matchQuery가 없을때 should쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                shouldQuery {
                    queries[
                        matchQuery(
                            field = "a",
                            query = ""
                        ),
                        matchQuery(
                            field = "b",
                            query = null
                        )
                    ]
                }
            }
        }

        val shouldQuery = query.bool().should()

        query.isBool shouldBe true
        shouldQuery.size shouldBe 0
    }

    test("match 쿼리에 boost 설정시 적용이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchQuery(
                        field = "a",
                        query = "1111",
                        boost = 2.0F
                    )
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isMatch }.find { it.match().field() == "a" }?.match()?.query()?.stringValue() shouldBe "1111"
        mustQuery.filter { it.isMatch }.find { it.match().field() == "a" }?.match()?.boost() shouldBe 2.0F
    }

    test("match 쿼리에 _name이 설정되면 match.queryName에 반영되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchQuery(
                        field = "a",
                        query = "1111",
                        _name = "named"
                    )
                }
            }
        }

        val mustList = query.bool().must()

        query.isBool shouldBe true
        mustList.size shouldBe 1

        val match = mustList.first().match()
        match.field() shouldBe "a"
        match.query().stringValue() shouldBe "1111"
        match.queryName() shouldBe "named"
    }

    test("match 쿼리에서 operator, minimum_should_match, analyzer 설정이 적용되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchQuery(
                        field = "title",
                        query = "quick brown fox",
                        operator = Operator.And,
                        minimumShouldMatch = "2",
                        analyzer = "standard"
                    )
                }
            }
        }

        val m = query.bool().must().first().match()
        m.operator() shouldBe Operator.And
        m.minimumShouldMatch() shouldBe "2"
        m.analyzer() shouldBe "standard"
    }

    test("match 쿼리에서 zero_terms_query, lenient, auto_generate_synonyms_phrase_query 설정이 적용되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchQuery(
                        field = "content",
                        query = "the",
                        zeroTermsQuery = ZeroTermsQuery.All,
                        lenient = true,
                        autoGenerateSynonymsPhraseQuery = true
                    )
                }
            }
        }

        val m = query.bool().must().first().match()
        m.zeroTermsQuery() shouldBe ZeroTermsQuery.All
        m.lenient() shouldBe true
        m.autoGenerateSynonymsPhraseQuery() shouldBe true
    }

    test("match 쿼리에서 fuzziness 관련 옵션 설정이 적용되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchQuery(
                        field = "name",
                        query = "jon",
                        fuzziness = "AUTO",
                        prefixLength = 1,
                        maxExpansions = 50,
                        fuzzyTranspositions = true,
                        fuzzyRewrite = "constant_score"
                    )
                }
            }
        }

        val must = query.bool().must().first().match()
        must.fuzziness() shouldBe "AUTO"
        must.prefixLength() shouldBe 1
        must.maxExpansions() shouldBe 50
        must.fuzzyTranspositions() shouldBe true
        must.fuzzyRewrite() shouldBe "constant_score"
    }
})

