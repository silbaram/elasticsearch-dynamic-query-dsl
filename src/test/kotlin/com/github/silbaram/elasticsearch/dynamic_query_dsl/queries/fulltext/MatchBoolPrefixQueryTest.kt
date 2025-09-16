package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MatchBoolPrefixQueryTest : FunSpec({

    test("최상위 match_bool_prefix 쿼리 생성이 되어야함") {
        val query = query {
            matchBoolPrefix(
                field = "message",
                query = "quick brown f"
            )
        }

        query.isMatchBoolPrefix shouldBe true
        query.matchBoolPrefix().field() shouldBe "message"
        query.matchBoolPrefix().query() shouldBe "quick brown f"
    }

    test("must 쿼리에서 match_bool_prefix 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        { matchBoolPrefix(field = "title", query = "quick brow") },
                        { matchBoolPrefix(field = "desc", query = "kotlin dsl") }
                    ]
                }
            }
        }

        val must = query.bool().must()
        query.isBool shouldBe true
        must.size shouldBe 2
        must.filter { it.isMatchBoolPrefix }.find { it.matchBoolPrefix().field() == "title" }?.matchBoolPrefix()?.query() shouldBe "quick brow"
        must.filter { it.isMatchBoolPrefix }.find { it.matchBoolPrefix().field() == "desc" }?.matchBoolPrefix()?.query() shouldBe "kotlin dsl"
    }

    test("must 쿼리에서 query가 null/빈 문자열이면 제외되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull { matchBoolPrefix(field = "a", query = null) },
                        com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull { matchBoolPrefix(field = "b", query = "") },
                        query { matchBoolPrefix(field = "c", query = "hello w") }
                    ]
                }
            }
        }

        val must = query.bool().must()
        query.isBool shouldBe true
        must.size shouldBe 1
        must.first().matchBoolPrefix().field() shouldBe "c"
        must.first().matchBoolPrefix().query() shouldBe "hello w"
    }

    test("filter/mustNot/should 절에서도 동일하게 동작해야함") {
        val query = query {
            boolQuery {
                filterQuery { queries[ { matchBoolPrefix(field = "a", query = "abc d") } ] }
                mustNotQuery { queries[ { matchBoolPrefix(field = "b", query = "efg h") } ] }
                shouldQuery { queries[ { matchBoolPrefix(field = "c", query = "ijk l") } ] }
            }
        }

        val filter = query.bool().filter()
        val mustNot = query.bool().mustNot()
        val should = query.bool().should()

        filter.size shouldBe 1
        mustNot.size shouldBe 1
        should.size shouldBe 1

        filter.first().matchBoolPrefix().field() shouldBe "a"
        mustNot.first().matchBoolPrefix().field() shouldBe "b"
        should.first().matchBoolPrefix().field() shouldBe "c"
    }

    test("옵션: operator, minimum_should_match, analyzer, boost, _name 적용 확인") {
        val query = query {
            boolQuery {
                mustQuery {
                    query {
                        matchBoolPrefix(
                            field = "title",
                            query = "quick brown f",
                            operator = Operator.And,
                            minimumShouldMatch = "2",
                            analyzer = "standard",
                            fuzziness = "AUTO",
                            prefixLength = 1,
                            maxExpansions = 50,
                            fuzzyTranspositions = true,
                            fuzzyRewrite = "constant_score",
                            boost = 1.5F,
                            _name = "named"
                        )
                    }
                }
            }
        }

        val qb = query.bool().must().first().matchBoolPrefix()
        qb.field() shouldBe "title"
        qb.query() shouldBe "quick brown f"
        qb.operator() shouldBe Operator.And
        qb.minimumShouldMatch() shouldBe "2"
        qb.analyzer() shouldBe "standard"
        qb.fuzziness() shouldBe "AUTO"
        qb.prefixLength() shouldBe 1
        qb.maxExpansions() shouldBe 50
        qb.fuzzyTranspositions() shouldBe true
        qb.fuzzyRewrite() shouldBe "constant_score"
        qb.boost() shouldBe 1.5F
        qb.queryName() shouldBe "named"
    }
})
