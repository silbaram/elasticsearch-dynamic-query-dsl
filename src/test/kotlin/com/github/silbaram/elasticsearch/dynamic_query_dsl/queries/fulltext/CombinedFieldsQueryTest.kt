package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.CombinedFieldsOperator
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CombinedFieldsQueryTest : FunSpec({

    test("최상위 combined_fields 쿼리 생성") {
        val q = query {
            combinedFields(
                query = "quick brown fox",
                fields = listOf("title^2", "body")
            )
        }

        q.isCombinedFields shouldBe true
        val cf = q.combinedFields()
        cf.query() shouldBe "quick brown fox"
        cf.fields().size shouldBe 2
        cf.fields().contains("title^2") shouldBe true
        cf.fields().contains("body") shouldBe true
    }

    test("must 절에서 combined_fields 생성/생략 동작") {
        val q = query {
            boolQuery {
                mustQuery {
                    queries[
                        { combinedFields(query = "kotlin coroutine", fields = listOf("title", "desc")) },
                        { combinedFields(query = null, fields = listOf("title")) },
                        { combinedFields(query = "", fields = listOf("title")) },
                        { combinedFields(query = "text", fields = emptyList()) }
                    ]
                }
            }
        }

        val must = q.bool().must()
        must.size shouldBe 1
        val cf = must.first().combinedFields()
        cf.query() shouldBe "kotlin coroutine"
        cf.fields().size shouldBe 2
    }

    test("옵션: operator/minimum_should_match/auto_synonyms/boost/_name 적용") {
        val q = query {
            boolQuery {
                mustQuery {
                    combinedFields(
                        query = "john smith",
                        fields = listOf("first_name", "last_name"),
                        operator = CombinedFieldsOperator.And,
                        minimumShouldMatch = "2",
                        autoGenerateSynonymsPhraseQuery = true,
                        boost = 1.2f,
                        _name = "cf_named"
                    )
                }
            }
        }

        val cf = q.bool().must().first().combinedFields()
        cf.operator() shouldBe CombinedFieldsOperator.And
        cf.minimumShouldMatch() shouldBe "2"
        cf.autoGenerateSynonymsPhraseQuery() shouldBe true
        cf.boost() shouldBe 1.2f
        cf.queryName() shouldBe "cf_named"
    }
})
