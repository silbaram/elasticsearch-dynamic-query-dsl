package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MultiMatchPhraseQueryTest : FunSpec({

    test("최상위 multi_match(type=phrase) 쿼리 생성") {
        val q = query {
            multiMatchPhrase(
                query = "quick brown fox",
                fields = listOf("title^2", "body")
            )
        }

        q.isMultiMatch shouldBe true
        val mm = q.multiMatch()
        mm.query() shouldBe "quick brown fox"
        mm.type() shouldBe TextQueryType.Phrase
        mm.fields().size shouldBe 2
        mm.fields().contains("title^2") shouldBe true
        mm.fields().contains("body") shouldBe true
    }

    test("must 절에서 multi_match phrase 생성/생략 동작") {
        val q = query {
            boolQuery {
                mustQuery {
                    queries[
                        { multiMatchPhrase(query = "kotlin coroutine", fields = listOf("title", "desc")) },
                        { multiMatchPhrase(query = null, fields = listOf("title")) },
                        { multiMatchPhrase(query = "", fields = listOf("title")) },
                        { multiMatchPhrase(query = "text", fields = emptyList()) }
                    ]
                }
            }
        }

        val must = q.bool().must()
        must.size shouldBe 1
        val mm = must.first().multiMatch()
        mm.type() shouldBe TextQueryType.Phrase
        mm.query() shouldBe "kotlin coroutine"
        mm.fields().size shouldBe 2
    }

    test("옵션: analyzer/slop/zero_terms_query/boost/_name 적용") {
        val q = query {
            boolQuery {
                mustQuery {
                    multiMatchPhrase(
                        query = "quick brown fox",
                        fields = listOf("title^2", "body"),
                        analyzer = "standard",
                        slop = 3,
                        zeroTermsQuery = ZeroTermsQuery.All,
                        boost = 1.4f,
                        _name = "mm_phrase"
                    )
                }
            }
        }

        val mm = q.bool().must().first().multiMatch()
        mm.type() shouldBe TextQueryType.Phrase
        mm.analyzer() shouldBe "standard"
        mm.slop() shouldBe 3
        mm.zeroTermsQuery() shouldBe ZeroTermsQuery.All
        mm.boost() shouldBe 1.4f
        mm.queryName() shouldBe "mm_phrase"
    }
})
