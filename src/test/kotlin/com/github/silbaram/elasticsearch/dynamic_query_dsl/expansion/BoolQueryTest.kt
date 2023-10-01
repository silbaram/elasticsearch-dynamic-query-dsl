package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.shouldQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BoolQueryTest: FunSpec({
    context("bool에 query 추가 생성 테스트") {
        test("filter query 추가 테스트") {
            val boolQuery = Query.Builder().boolQuery {
                filterQuery(
                    Query.Builder().term(TermQuery.Builder().field("field1").value("value1").build()).build()
                )
            }

            val boolQueryBuild = boolQuery.build()

            boolQueryBuild.isBool shouldBe true
            boolQueryBuild.bool().filter().size shouldBe 1
            boolQueryBuild.bool().must().size shouldBe 0
            boolQueryBuild.bool().mustNot().size shouldBe 0
            boolQueryBuild.bool().should().size shouldBe 0
        }

        test("must query 추가 테스트") {
            val boolQuery = Query.Builder().boolQuery {
                mustQuery(
                    Query.Builder().term(TermQuery.Builder().field("field1").value("value1").build()).build()
                )
            }

            val boolQueryBuild = boolQuery.build()

            boolQueryBuild.isBool shouldBe true
            boolQueryBuild.bool().filter().size shouldBe 0
            boolQueryBuild.bool().must().size shouldBe 1
            boolQueryBuild.bool().mustNot().size shouldBe 0
            boolQueryBuild.bool().should().size shouldBe 0
        }

        test("mustNot query 추가 테스트") {
            val boolQuery = Query.Builder().boolQuery {
                mustNotQuery(
                    Query.Builder().term(TermQuery.Builder().field("field1").value("value1").build()).build()
                )
            }

            val boolQueryBuild = boolQuery.build()

            boolQueryBuild.isBool shouldBe true
            boolQueryBuild.bool().filter().size shouldBe 0
            boolQueryBuild.bool().must().size shouldBe 0
            boolQueryBuild.bool().mustNot().size shouldBe 1
            boolQueryBuild.bool().should().size shouldBe 0
        }

        test("should query 추가 테스트") {
            val boolQuery = Query.Builder().boolQuery {
                shouldQuery(
                    Query.Builder().term(TermQuery.Builder().field("field1").value("value1").build()).build()
                )
            }

            val boolQueryBuild = boolQuery.build()

            boolQueryBuild.isBool shouldBe true
            boolQueryBuild.bool().filter().size shouldBe 0
            boolQueryBuild.bool().must().size shouldBe 0
            boolQueryBuild.bool().mustNot().size shouldBe 0
            boolQueryBuild.bool().should().size shouldBe 1
        }
    }
})