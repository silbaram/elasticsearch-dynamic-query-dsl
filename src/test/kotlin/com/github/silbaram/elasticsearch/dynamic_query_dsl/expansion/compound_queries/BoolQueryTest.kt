package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.shouldQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BoolQueryTest: FunSpec({

    test("bool에 filter query가 추가 되어야함") {
        val boolQuery = Query.Builder().boolQuery {
            filterQuery {
                termQuery(
                    field = "field1",
                    value = "value1"
                )
            }
        }

        val boolQueryBuild = boolQuery.build()

        boolQueryBuild.isBool shouldBe true
        boolQueryBuild.bool().filter().size shouldBe 1
        boolQueryBuild.bool().must().size shouldBe 0
        boolQueryBuild.bool().mustNot().size shouldBe 0
        boolQueryBuild.bool().should().size shouldBe 0
    }

    test("bool에 must query가 추가 되어야함") {
        val boolQuery = Query.Builder().boolQuery {
            mustQuery {
                termQuery(
                    field = "field1",
                    value = "value1"
                )
            }
        }

        val boolQueryBuild = boolQuery.build()

        boolQueryBuild.isBool shouldBe true
        boolQueryBuild.bool().filter().size shouldBe 0
        boolQueryBuild.bool().must().size shouldBe 1
        boolQueryBuild.bool().mustNot().size shouldBe 0
        boolQueryBuild.bool().should().size shouldBe 0
    }

    test("bool에 mustNot query가 추가 되어야함") {
        val boolQuery = Query.Builder().boolQuery {
            mustNotQuery {
                termQuery(
                    field = "field1",
                    value = "value1"
                )
            }
        }

        val boolQueryBuild = boolQuery.build()

        boolQueryBuild.isBool shouldBe true
        boolQueryBuild.bool().filter().size shouldBe 0
        boolQueryBuild.bool().must().size shouldBe 0
        boolQueryBuild.bool().mustNot().size shouldBe 1
        boolQueryBuild.bool().should().size shouldBe 0
    }

    test("bool에 should query가 추가 되어야함") {
        val boolQuery = Query.Builder().boolQuery {
            shouldQuery {
                termQuery(
                    field = "field1",
                    value = "value1"
                )
            }
        }

        val boolQueryBuild = boolQuery.build()

        boolQueryBuild.isBool shouldBe true
        boolQueryBuild.bool().filter().size shouldBe 0
        boolQueryBuild.bool().must().size shouldBe 0
        boolQueryBuild.bool().mustNot().size shouldBe 0
        boolQueryBuild.bool().should().size shouldBe 1
    }

    test("bool에 filter, must, mustNot, should query가 추가 되어야함") {
        val boolQuery = Query.Builder().boolQuery {
            filterQuery {
                termQuery(
                    field = "field1",
                    value = "value1"
                )
            }
            mustQuery {
                termQuery(
                    field = "field1",
                    value = "value1"
                )
            }
            mustNotQuery {
                termQuery(
                    field = "field1",
                    value = "value1"
                )
            }
            shouldQuery {
                termQuery(
                    field = "field1",
                    value = "value1"
                )
            }
        }

        val boolQueryBuild = boolQuery.build()

        boolQueryBuild.isBool shouldBe true
        boolQueryBuild.bool().filter().size shouldBe 1
        boolQueryBuild.bool().must().size shouldBe 1
        boolQueryBuild.bool().mustNot().size shouldBe 1
        boolQueryBuild.bool().should().size shouldBe 1
    }

    test("bool에 minimumShouldMatch, boost가 추가 되어야함") {
        val boolQuery = Query.Builder().boolQuery {
            filterQuery {
                termQuery(
                    field = "field1",
                    value = "value1"
                )
            }
            mustQuery {
                termQuery(
                    field = "field1",
                    value = "value1"
                )
            }
            mustNotQuery {
                termQuery(
                    field = "field1",
                    value = "value1"
                )
            }
            shouldQuery {
                termQuery(
                    field = "field1",
                    value = "value1"
                )
            }
            minimumShouldMatch("2")
            boost(2.0F)
        }

        val boolQueryBuild = boolQuery.build()

        boolQueryBuild.isBool shouldBe true
        boolQueryBuild.bool().filter().size shouldBe 1
        boolQueryBuild.bool().must().size shouldBe 1
        boolQueryBuild.bool().mustNot().size shouldBe 1
        boolQueryBuild.bool().should().size shouldBe 1
        boolQueryBuild.bool().minimumShouldMatch() shouldBe "2"
        boolQueryBuild.bool().boost() shouldBe 2.0F
    }
})