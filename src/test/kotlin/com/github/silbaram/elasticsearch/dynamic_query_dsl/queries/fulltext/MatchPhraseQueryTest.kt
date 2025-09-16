package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MatchPhraseQueryTest : FunSpec({

    test("must 쿼리에서 match_phrase 쿼리 생성/스킵 동작") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        query { matchPhrase(field = "message", query = "this is a test") },
                        queryOrNull { matchPhrase(field = "skip_null", query = null) },
                        queryOrNull { matchPhrase(field = "skip_blank", query = "") }
                    ]
                }
            }
        }

        val must = query.bool().must()
        query.isBool shouldBe true
        must.size shouldBe 1
        must.first().isMatchPhrase shouldBe true
        must.first().matchPhrase().field() shouldBe "message"
        must.first().matchPhrase().query() shouldBe "this is a test"
    }

    test("filter/mustNot/should 에서도 match_phrase 쿼리 동작") {
        val query = query {
            boolQuery {
                filterQuery { queries[ { matchPhrase(field = "f", query = "alpha beta") } ] }
                mustNotQuery { queries[ { matchPhrase(field = "mn", query = "gamma delta") } ] }
                shouldQuery { queries[ { matchPhrase(field = "s", query = "quick brown fox") } ] }
            }
        }

        query.bool().filter().size shouldBe 1
        query.bool().filter().first().matchPhrase().field() shouldBe "f"

        query.bool().mustNot().size shouldBe 1
        query.bool().mustNot().first().matchPhrase().field() shouldBe "mn"

        query.bool().should().size shouldBe 1
        query.bool().should().first().matchPhrase().field() shouldBe "s"
    }

    test("match_phrase 옵션: analyzer, slop, zero_terms_query, boost, _name 적용") {
        val query = query {
            boolQuery {
                mustQuery {
                    query {
                        matchPhrase(
                        field = "title",
                        query = "quick brown fox",
                        analyzer = "standard",
                        slop = 2,
                        zeroTermsQuery = ZeroTermsQuery.All,
                        boost = 1.5F,
                        _name = "phrase_query"
                        )
                    }
                }
            }
        }

        val mp = query.bool().must().first().matchPhrase()
        mp.field() shouldBe "title"
        mp.query() shouldBe "quick brown fox"
        mp.analyzer() shouldBe "standard"
        mp.slop() shouldBe 2
        mp.zeroTermsQuery() shouldBe ZeroTermsQuery.All
        mp.boost() shouldBe 1.5F
        mp.queryName() shouldBe "phrase_query"
    }
})
