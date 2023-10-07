package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.shouldQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TermQueryTest: FunSpec({

    test("must 쿼리에서 term 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
                    termQuery(
                        field = "a",
                        value = "1111"
                    ),
                    termQuery(
                        field = "b",
                        value = "2222"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 2
        mustQuery.filter { it.isTerm }.find { it.term().field() == "a" }?.term()?.value()?.stringValue() shouldBe "1111"
        mustQuery.filter { it.isTerm }.find { it.term().field() == "b" }?.term()?.value()?.stringValue() shouldBe "2222"
    }

    test("must 쿼리에서 termQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
                    termQuery(
                        field = "a",
                        value = null
                    ),
                    termQuery(
                        field = "b",
                        value = ""
                    ),
                    termQuery(
                        field = "c",
                        value = "3333"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isTerm }.find { it.term().field() == "a" } shouldBe null
        mustQuery.filter { it.isTerm }.find { it.term().field() == "b" } shouldBe null
        mustQuery.filter { it.isTerm }.find { it.term().field() == "c" }?.term()?.value()?.stringValue() shouldBe "3333"
    }

    test("must 쿼리에서 termQuery가 없을때 must쿼리는 생성 안되야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
                    termQuery(
                        field = "a",
                        value = ""
                    ),
                    termQuery(
                        field = "b",
                        value = null
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 0
    }

    test("filter 쿼리에서 term 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                filterQuery(
                    termQuery(
                        field = "a",
                        value = "1111"
                    ),
                    termQuery(
                        field = "b",
                        value = "2222"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val filterQuery = boolQueryBuild.bool().filter()

        boolQueryBuild.isBool shouldBe true
        filterQuery.size shouldBe 2
        filterQuery.filter { it.isTerm }.find { it.term().field() == "a" }?.term()?.value()?.stringValue() shouldBe "1111"
        filterQuery.filter { it.isTerm }.find { it.term().field() == "b" }?.term()?.value()?.stringValue() shouldBe "2222"
    }

    test("filter 쿼리에서 termQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                filterQuery(
                    termQuery(
                        field = "a",
                        value = null
                    ),
                    termQuery(
                        field = "b",
                        value = ""
                    ),
                    termQuery(
                        field = "c",
                        value = "3333"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val filterQuery = boolQueryBuild.bool().filter()

        boolQueryBuild.isBool shouldBe true
        filterQuery.size shouldBe 1
        filterQuery.filter { it.isTerm }.find { it.term().field() == "a" } shouldBe null
        filterQuery.filter { it.isTerm }.find { it.term().field() == "b" } shouldBe null
        filterQuery.filter { it.isTerm }.find { it.term().field() == "c" }?.term()?.value()?.stringValue() shouldBe "3333"
    }

    test("filter 쿼리에서 termQuery가 없을때 must쿼리는 생성 안되야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                filterQuery(
                    termQuery(
                        field = "a",
                        value = ""
                    ),
                    termQuery(
                        field = "b",
                        value = null
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val filterQuery = boolQueryBuild.bool().filter()

        boolQueryBuild.isBool shouldBe true
        filterQuery.size shouldBe 0
    }

    test("mustNot 쿼리에서 term 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustNotQuery(
                    termQuery(
                        field = "a",
                        value = "1111"
                    ),
                    termQuery(
                        field = "b",
                        value = "2222"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustNotQuery = boolQueryBuild.bool().mustNot()

        boolQueryBuild.isBool shouldBe true
        mustNotQuery.size shouldBe 2
        mustNotQuery.filter { it.isTerm }.find { it.term().field() == "a" }?.term()?.value()?.stringValue() shouldBe "1111"
        mustNotQuery.filter { it.isTerm }.find { it.term().field() == "b" }?.term()?.value()?.stringValue() shouldBe "2222"
    }

    test("mustNot 쿼리에서 termQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustNotQuery(
                    termQuery(
                        field = "a",
                        value = null
                    ),
                    termQuery(
                        field = "b",
                        value = ""
                    ),
                    termQuery(
                        field = "c",
                        value = "3333"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustNotQuery = boolQueryBuild.bool().mustNot()

        boolQueryBuild.isBool shouldBe true
        mustNotQuery.size shouldBe 1
        mustNotQuery.filter { it.isTerm }.find { it.term().field() == "a" } shouldBe null
        mustNotQuery.filter { it.isTerm }.find { it.term().field() == "b" } shouldBe null
        mustNotQuery.filter { it.isTerm }.find { it.term().field() == "c" }?.term()?.value()?.stringValue() shouldBe "3333"
    }

    test("mustNot 쿼리에서 termQuery가 없을때 must쿼리는 생성 안되야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustNotQuery(
                    termQuery(
                        field = "a",
                        value = ""
                    ),
                    termQuery(
                        field = "b",
                        value = null
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustNotQuery = boolQueryBuild.bool().mustNot()

        boolQueryBuild.isBool shouldBe true
        mustNotQuery.size shouldBe 0
    }

    test("should 쿼리에서 term 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                shouldQuery(
                    termQuery(
                        field = "a",
                        value = "1111"
                    ),
                    termQuery(
                        field = "b",
                        value = "2222"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val shouldQuery = boolQueryBuild.bool().should()

        boolQueryBuild.isBool shouldBe true
        shouldQuery.size shouldBe 2
        shouldQuery.filter { it.isTerm }.find { it.term().field() == "a" }?.term()?.value()?.stringValue() shouldBe "1111"
        shouldQuery.filter { it.isTerm }.find { it.term().field() == "b" }?.term()?.value()?.stringValue() shouldBe "2222"
    }

    test("should 쿼리에서 termQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                shouldQuery(
                    termQuery(
                        field = "a",
                        value = null
                    ),
                    termQuery(
                        field = "b",
                        value = ""
                    ),
                    termQuery(
                        field = "c",
                        value = "3333"
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val shouldQuery = boolQueryBuild.bool().should()

        boolQueryBuild.isBool shouldBe true
        shouldQuery.size shouldBe 1
        shouldQuery.filter { it.isTerm }.find { it.term().field() == "a" } shouldBe null
        shouldQuery.filter { it.isTerm }.find { it.term().field() == "b" } shouldBe null
        shouldQuery.filter { it.isTerm }.find { it.term().field() == "c" }?.term()?.value()?.stringValue() shouldBe "3333"
    }

    test("should 쿼리에서 termQuery가 없을때 must쿼리는 생성 안되야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustNotQuery(
                    termQuery(
                        field = "a",
                        value = ""
                    ),
                    termQuery(
                        field = "b",
                        value = null
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val shouldQuery = boolQueryBuild.bool().should()

        boolQueryBuild.isBool shouldBe true
        shouldQuery.size shouldBe 0
    }
})