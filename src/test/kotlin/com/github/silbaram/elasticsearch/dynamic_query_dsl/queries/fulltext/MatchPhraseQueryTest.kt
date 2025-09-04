package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MatchPhraseQueryTest : FunSpec({

    test("must 쿼리에서 match_phrase 쿼리 생성/스킵 동작") {
        val q = query {
            boolQuery {
                mustQuery {
                    queries[
                        matchPhraseQuery("message", "this is a test"),
                        matchPhraseQuery("skip_null", null),
                        matchPhraseQuery("skip_blank", "")
                    ]
                }
            }
        }

        val must = q.bool().must()
        q.isBool shouldBe true
        must.size shouldBe 1
        must.first().isMatchPhrase shouldBe true
        must.first().matchPhrase().field() shouldBe "message"
        must.first().matchPhrase().query() shouldBe "this is a test"
    }

    test("filter/mustNot/should 에서도 match_phrase 쿼리 동작") {
        val q = query {
            boolQuery {
                filterQuery {
                    queries[ matchPhraseQuery("f", "alpha beta") ]
                }
                mustNotQuery {
                    queries[ matchPhraseQuery("mn", "gamma delta") ]
                }
                shouldQuery {
                    queries[ matchPhraseQuery("s", "quick brown fox") ]
                }
            }
        }

        q.bool().filter().size shouldBe 1
        q.bool().filter().first().matchPhrase().field() shouldBe "f"

        q.bool().mustNot().size shouldBe 1
        q.bool().mustNot().first().matchPhrase().field() shouldBe "mn"

        q.bool().should().size shouldBe 1
        q.bool().should().first().matchPhrase().field() shouldBe "s"
    }

    test("match_phrase 옵션: analyzer, slop, zero_terms_query, boost, _name 적용") {
        val q = query {
            boolQuery {
                mustQuery {
                    matchPhraseQuery(
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

        val mp = q.bool().must().first().matchPhrase()
        mp.field() shouldBe "title"
        mp.query() shouldBe "quick brown fox"
        mp.analyzer() shouldBe "standard"
        mp.slop() shouldBe 2
        mp.zeroTermsQuery() shouldBe ZeroTermsQuery.All
        mp.boost() shouldBe 1.5F
        mp.queryName() shouldBe "phrase_query"
    }
})
