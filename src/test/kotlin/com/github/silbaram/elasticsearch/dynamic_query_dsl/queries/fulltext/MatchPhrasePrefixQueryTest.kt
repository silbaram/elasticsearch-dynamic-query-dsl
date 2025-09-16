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

class MatchPhrasePrefixQueryTest : FunSpec({

    test("최상위 match_phrase_prefix 쿼리 생성") {
        val query = query {
            matchPhrasePrefix(
                field = "path",
                query = "/api/ad"
            )
        }

        query.isMatchPhrasePrefix shouldBe true
        query.matchPhrasePrefix().field() shouldBe "path"
        query.matchPhrasePrefix().query() shouldBe "/api/ad"
    }

    test("must/filter/mustNot/should에서 생성/생략 동작") {
        val query = query {
            boolQuery {
                mustQuery { queries[ { matchPhrasePrefix(field = "a", query = "quick bro") } ] }
                filterQuery { queries[ { matchPhrasePrefix(field = "b", query = "fox ju") } ] }
                mustNotQuery { queries[ { matchPhrasePrefix(field = "c", query = "over th") } ] }
                shouldQuery { queries[ { matchPhrasePrefix(field = "d", query = "lazy do") } ] }
            }
        }

        query.bool().must().first().matchPhrasePrefix().field() shouldBe "a"
        query.bool().filter().first().matchPhrasePrefix().field() shouldBe "b"
        query.bool().mustNot().first().matchPhrasePrefix().field() shouldBe "c"
        query.bool().should().first().matchPhrasePrefix().field() shouldBe "d"
    }

    test("옵션 slop/zero_terms_query/max_expansions/boost/_name 적용") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchPhrasePrefix(
                        field = "title",
                        query = "kotlin cor",
                        slop = 2,
                        zeroTermsQuery = ZeroTermsQuery.None,
                        maxExpansions = 10,
                        boost = 1.2f,
                        _name = "mpp"
                    )
                }
            }
        }
        println("query = $query")
        val mpp = query.bool().must().first().matchPhrasePrefix()
        mpp.field() shouldBe "title"
        mpp.query() shouldBe "kotlin cor"
        mpp.slop() shouldBe 2
        mpp.zeroTermsQuery() shouldBe ZeroTermsQuery.None
        mpp.maxExpansions() shouldBe 10
        mpp.boost() shouldBe 1.2f
        mpp.queryName() shouldBe "mpp"
    }
})
