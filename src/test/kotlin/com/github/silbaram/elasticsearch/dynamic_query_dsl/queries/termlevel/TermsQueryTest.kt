package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termsQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class TermsQueryTest : FunSpec({

    test("must 쿼리에서 terms 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        termsQuery(
                            field = "a",
                            values = listOf("1111", "2222")
                        ),
                        termsQuery(
                            field = "b",
                            values = listOf("3333", "4444")
                        )
                    ]
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 2
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "b" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("3333", "4444")
    }

    test("must 쿼리에서 termsQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        termsQuery(
                            field = "a",
                            values = null
                        ),
                        termsQuery(
                            field = "b",
                            values = emptyList()
                        ),
                        termsQuery(
                            field = "c",
                            values = listOf("1111", "2222")
                        ),
                        termsQuery(
                            field = "d",
                            values = listOf(null, "3333")
                        )
                    ]
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 2
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "a" } shouldBe null
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "b" } shouldBe null
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "c" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "d" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("3333")
    }

    test("must 쿼리에서 termsQuery가 없을때 must쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        termsQuery(
                            field = "a",
                            values = null
                        ),
                        termsQuery(
                            field = "b",
                            values = emptyList()
                        )
                    ]
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 0
    }

    test("filter 쿼리에서 terms 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                filterQuery{
                    queries[
                        termsQuery(
                            field = "a",
                            values = listOf("1111", "2222")
                        ),
                        termsQuery(
                            field = "b",
                            values = listOf("3333", "4444")
                        )
                    ]
                }
            }
        }
        val filterQuery = query.bool().filter()

        query.isBool shouldBe true
        filterQuery.size shouldBe 2
        filterQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        filterQuery.filter { it.isTerms }.find { it.terms().field() == "b" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("3333", "4444")
    }

    test("filter 쿼리에서 termsQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                filterQuery {
                    queries[
                        termsQuery(
                            field = "a",
                            values = null
                        ),
                        termsQuery(
                            field = "b",
                            values = emptyList()
                        ),
                        termsQuery(
                            field = "c",
                            values = listOf("1111", "2222")
                        ),
                        termsQuery(
                            field = "d",
                            values = listOf(null, "3333")
                        )
                    ]
                }
            }
        }
        val filterQuery = query.bool().filter()

        query.isBool shouldBe true
        filterQuery.size shouldBe 2
        filterQuery.filter { it.isTerms }.find { it.terms().field() == "a" } shouldBe null
        filterQuery.filter { it.isTerms }.find { it.terms().field() == "b" } shouldBe null
        filterQuery.filter { it.isTerms }.find { it.terms().field() == "c" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        filterQuery.filter { it.isTerms }.find { it.terms().field() == "d" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("3333")
    }

    test("filter 쿼리에서 termsQuery가 없을때 filter쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                filterQuery {
                    queries[
                        termsQuery(
                            field = "a",
                            values = null
                        ),
                        termsQuery(
                            field = "b",
                            values = emptyList()
                        )
                    ]
                }
            }
        }

        val filterQuery = query.bool().filter()

        query.isBool shouldBe true
        filterQuery.size shouldBe 0
    }

    test("mustNot 쿼리에서 terms 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                mustNotQuery {
                    queries[
                        termsQuery(
                            field = "a",
                            values = listOf("1111", "2222")
                        ),
                        termsQuery(
                            field = "b",
                            values = listOf("3333", "4444")
                        )
                    ]
                }
            }
        }
        val mustNotQuery = query.bool().mustNot()

        query.isBool shouldBe true
        mustNotQuery.size shouldBe 2
        mustNotQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        mustNotQuery.filter { it.isTerms }.find { it.terms().field() == "b" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("3333", "4444")
    }

    test("mustNot 쿼리에서 termsQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                mustNotQuery {
                    queries[
                        termsQuery(
                            field = "a",
                            values = null
                        ),
                        termsQuery(
                            field = "b",
                            values = emptyList()
                        ),
                        termsQuery(
                            field = "c",
                            values = listOf("1111", "2222")
                        ),
                        termsQuery(
                            field = "d",
                            values = listOf(null, "3333")
                        )
                    ]
                }
            }
        }
        val mustNotQuery = query.bool().mustNot()

        query.isBool shouldBe true
        mustNotQuery.size shouldBe 2
        mustNotQuery.filter { it.isTerms }.find { it.terms().field() == "a" } shouldBe null
        mustNotQuery.filter { it.isTerms }.find { it.terms().field() == "b" } shouldBe null
        mustNotQuery.filter { it.isTerms }.find { it.terms().field() == "c" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        mustNotQuery.filter { it.isTerms }.find { it.terms().field() == "d" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("3333")
    }

    test("mustNot 쿼리에서 termsQuery가 없을때 filter쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                filterQuery {
                    queries[
                        termsQuery(
                            field = "a",
                            values = null
                        ),
                        termsQuery(
                            field = "b",
                            values = emptyList()
                        )
                    ]
                }
            }
        }
        val mustNotQuery = query.bool().mustNot()

        query.isBool shouldBe true
        mustNotQuery.size shouldBe 0
    }

    test("should 쿼리에서 terms 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                shouldQuery {
                    queries[
                        termsQuery(
                            field = "a",
                            values = listOf("1111", "2222")
                        ),
                        termsQuery(
                            field = "b",
                            values = listOf("3333", "4444")
                        )
                    ]
                }
            }
        }
        val shouldQuery = query.bool().should()

        query.isBool shouldBe true
        shouldQuery.size shouldBe 2
        shouldQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        shouldQuery.filter { it.isTerms }.find { it.terms().field() == "b" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("3333", "4444")
    }

    test("should 쿼리에서 termsQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                shouldQuery {
                    queries[
                        termsQuery(
                            field = "a",
                            values = null
                        ),
                        termsQuery(
                            field = "b",
                            values = emptyList()
                        ),
                        termsQuery(
                            field = "c",
                            values = listOf("1111", "2222")
                        ),
                        termsQuery(
                            field = "d",
                            values = listOf(null, "3333")
                        )
                    ]
                }
            }
        }
        val shouldQuery = query.bool().should()

        query.isBool shouldBe true
        shouldQuery.size shouldBe 2
        shouldQuery.filter { it.isTerms }.find { it.terms().field() == "a" } shouldBe null
        shouldQuery.filter { it.isTerms }.find { it.terms().field() == "b" } shouldBe null
        shouldQuery.filter { it.isTerms }.find { it.terms().field() == "c" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        shouldQuery.filter { it.isTerms }.find { it.terms().field() == "d" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("3333")
    }

    test("should 쿼리에서 termsQuery가 없을때 filter쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                shouldQuery {
                    queries[
                        termsQuery(
                            field = "a",
                            values = null
                        ),
                        termsQuery(
                            field = "b",
                            values = emptyList()
                        )
                    ]
                }
            }
        }
        val shouldQuery = query.bool().should()

        query.isBool shouldBe true
        shouldQuery.size shouldBe 0
    }

    test("terms 쿼리에 boost 설정시 적용이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    termsQuery(
                        field = "a",
                        values = listOf("1111", "2222"),
                        boost = 2.5F
                    )
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().boost() shouldBe 2.5F
    }

    test("terms 쿼리에 _name이 설정되면 terms.queryName에 반영되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    termsQuery(
                        field = "a",
                        values = listOf("1111", "2222"),
                        _name = "named"
                    )
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().terms().value().map { it._get() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().queryName() shouldBe "named"
    }
})
