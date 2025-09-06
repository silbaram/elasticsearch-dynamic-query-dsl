package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MultiMatchQueryTest : FunSpec({

    test("최상위 multi_match(best_fields) 쿼리 생성") {
        val q = query {
            multiMatch(
                query = "quick brown fox",
                fields = listOf("title^2", "body"),
                type = TextQueryType.BestFields,
                operator = Operator.Or,
                minimumShouldMatch = "2"
            )
        }

        q.isMultiMatch shouldBe true
        val mm = q.multiMatch()
        mm.query() shouldBe "quick brown fox"
        mm.type() shouldBe TextQueryType.BestFields
        mm.operator() shouldBe Operator.Or
        mm.minimumShouldMatch() shouldBe "2"
        mm.fields().size shouldBe 2
    }

    test("must 절에서 multi_match 생성/생략 동작") {
        val q = query {
            boolQuery {
                mustQuery {
                    queries[
                        multiMatchQuery("kotlin coroutine", listOf("title", "desc")),
                        multiMatchQuery(null, listOf("title")),
                        multiMatchQuery("", listOf("title")),
                        multiMatchQuery("text", emptyList())
                    ]
                }
            }
        }

        val must = q.bool().must()
        must.size shouldBe 1
        val mm = must.first().multiMatch()
        mm.query() shouldBe "kotlin coroutine"
        mm.fields().size shouldBe 2
    }

    test("cross_fields, phrase_prefix 옵션 및 부가 옵션 적용") {
        val q = query {
            boolQuery {
                mustQuery {
                    multiMatchQuery(
                        query = "john smith",
                        fields = listOf("first_name", "last_name"),
                        type = TextQueryType.CrossFields,
                        operator = Operator.And,
                        tieBreaker = 0.0,
                        lenient = true,
                        zeroTermsQuery = ZeroTermsQuery.All,
                        autoGenerateSynonymsPhraseQuery = true
                    )
                }
            }
        }

        val mm = q.bool().must().first().multiMatch()
        mm.type() shouldBe TextQueryType.CrossFields
        mm.operator() shouldBe Operator.And
        mm.tieBreaker() shouldBe 0.0
        mm.lenient() shouldBe true
        mm.zeroTermsQuery() shouldBe ZeroTermsQuery.All
        mm.autoGenerateSynonymsPhraseQuery() shouldBe true
    }

    test("phrase_prefix 타입에서 slop/max_expansions/prefixLength 설정") {
        val q = query {
            multiMatch(
                query = "quick bro",
                fields = listOf("title", "body"),
                type = TextQueryType.PhrasePrefix,
                slop = 2,
                maxExpansions = 50,
                prefixLength = 1
            )
        }

        val mm = q.multiMatch()
        mm.type() shouldBe TextQueryType.PhrasePrefix
        mm.slop() shouldBe 2
        mm.maxExpansions() shouldBe 50
        mm.prefixLength() shouldBe 1
    }
})
