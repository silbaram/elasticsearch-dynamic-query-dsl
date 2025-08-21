package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.shouldQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class TermsQueryTest: FunSpec({

    test("must 쿼리에서 terms 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
                    termsQuery(
                        field = "a",
                        values = listOf("1111", "2222")
                    ),
                    termsQuery(
                        field = "b",
                        values = listOf("3333", "4444")
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 2
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "b" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("3333", "4444")
    }

    test("must 쿼리에서 termsQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
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
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 2
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "a" } shouldBe null
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "b" } shouldBe null
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "c" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "d" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("3333")
    }

    test("must 쿼리에서 termsQuery가 없을때 must쿼리는 생성 안되야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
                    termsQuery(
                        field = "a",
                        values = null
                    ),
                    termsQuery(
                        field = "b",
                        values = emptyList()
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 0
    }

    test("filter 쿼리에서 terms 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                filterQuery(
                    termsQuery(
                        field = "a",
                        values = listOf("1111", "2222")
                    ),
                    termsQuery(
                        field = "b",
                        values = listOf("3333", "4444")
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val filterQuery = boolQueryBuild.bool().filter()

        boolQueryBuild.isBool shouldBe true
        filterQuery.size shouldBe 2
        filterQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        filterQuery.filter { it.isTerms }.find { it.terms().field() == "b" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("3333", "4444")
    }

    test("filter 쿼리에서 termsQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                filterQuery(
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
                )
            }

        val boolQueryBuild = boolQuery.build()
        val filterQuery = boolQueryBuild.bool().filter()

        boolQueryBuild.isBool shouldBe true
        filterQuery.size shouldBe 2
        filterQuery.filter { it.isTerms }.find { it.terms().field() == "a" } shouldBe null
        filterQuery.filter { it.isTerms }.find { it.terms().field() == "b" } shouldBe null
        filterQuery.filter { it.isTerms }.find { it.terms().field() == "c" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        filterQuery.filter { it.isTerms }.find { it.terms().field() == "d" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("3333")
    }

    test("filter 쿼리에서 termsQuery가 없을때 filter쿼리는 생성 안되야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                filterQuery(
                    termsQuery(
                        field = "a",
                        values = null
                    ),
                    termsQuery(
                        field = "b",
                        values = emptyList()
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val filterQuery = boolQueryBuild.bool().filter()

        boolQueryBuild.isBool shouldBe true
        filterQuery.size shouldBe 0
    }

    test("mustNot 쿼리에서 terms 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustNotQuery(
                    termsQuery(
                        field = "a",
                        values = listOf("1111", "2222")
                    ),
                    termsQuery(
                        field = "b",
                        values = listOf("3333", "4444")
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustNotQuery = boolQueryBuild.bool().mustNot()

        boolQueryBuild.isBool shouldBe true
        mustNotQuery.size shouldBe 2
        mustNotQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        mustNotQuery.filter { it.isTerms }.find { it.terms().field() == "b" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("3333", "4444")
    }

    test("mustNot 쿼리에서 termsQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustNotQuery(
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
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustNotQuery = boolQueryBuild.bool().mustNot()

        boolQueryBuild.isBool shouldBe true
        mustNotQuery.size shouldBe 2
        mustNotQuery.filter { it.isTerms }.find { it.terms().field() == "a" } shouldBe null
        mustNotQuery.filter { it.isTerms }.find { it.terms().field() == "b" } shouldBe null
        mustNotQuery.filter { it.isTerms }.find { it.terms().field() == "c" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        mustNotQuery.filter { it.isTerms }.find { it.terms().field() == "d" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("3333")
    }

    test("mustNot 쿼리에서 termsQuery가 없을때 filter쿼리는 생성 안되야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                filterQuery(
                    termsQuery(
                        field = "a",
                        values = null
                    ),
                    termsQuery(
                        field = "b",
                        values = emptyList()
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustNotQuery = boolQueryBuild.bool().mustNot()

        boolQueryBuild.isBool shouldBe true
        mustNotQuery.size shouldBe 0
    }

    test("should 쿼리에서 terms 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                shouldQuery(
                    termsQuery(
                        field = "a",
                        values = listOf("1111", "2222")
                    ),
                    termsQuery(
                        field = "b",
                        values = listOf("3333", "4444")
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val shouldQuery = boolQueryBuild.bool().should()

        boolQueryBuild.isBool shouldBe true
        shouldQuery.size shouldBe 2
        shouldQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        shouldQuery.filter { it.isTerms }.find { it.terms().field() == "b" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("3333", "4444")
    }

    test("should 쿼리에서 termsQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                shouldQuery(
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
                )
            }

        val boolQueryBuild = boolQuery.build()
        val shouldQuery = boolQueryBuild.bool().should()

        boolQueryBuild.isBool shouldBe true
        shouldQuery.size shouldBe 2
        shouldQuery.filter { it.isTerms }.find { it.terms().field() == "a" } shouldBe null
        shouldQuery.filter { it.isTerms }.find { it.terms().field() == "b" } shouldBe null
        shouldQuery.filter { it.isTerms }.find { it.terms().field() == "c" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        shouldQuery.filter { it.isTerms }.find { it.terms().field() == "d" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("3333")
    }

    test("should 쿼리에서 termsQuery가 없을때 filter쿼리는 생성 안되야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                shouldQuery(
                    termsQuery(
                        field = "a",
                        values = null
                    ),
                    termsQuery(
                        field = "b",
                        values = emptyList()
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val shouldQuery = boolQueryBuild.bool().should()

        boolQueryBuild.isBool shouldBe true
        shouldQuery.size shouldBe 0
    }

    test("terms 쿼리에 boost 설정시 적용이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
                    termsQuery(
                        field = "a",
                        values = listOf("1111", "2222"),
                        boost = 2.5F
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().terms().value().map { it.stringValue() } shouldContainExactlyInAnyOrder  listOf("1111", "2222")
        mustQuery.filter { it.isTerms }.find { it.terms().field() == "a" }!!.terms().boost() shouldBe 2.5F
    }
})