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
        val q = query {
            matchBoolPrefix(
                field = "message",
                query = "quick brown f"
            )
        }

        q.isMatchBoolPrefix shouldBe true
        q.matchBoolPrefix().field() shouldBe "message"
        q.matchBoolPrefix().query() shouldBe "quick brown f"
    }

    test("must 쿼리에서 match_bool_prefix 쿼리 생성이 되어야함") {
        val q = query {
            boolQuery {
                mustQuery {
                    queries[
                        matchBoolPrefixQuery(field = "title", query = "quick brow"),
                        matchBoolPrefixQuery(field = "desc", query = "kotlin dsl")
                    ]
                }
            }
        }

        val must = q.bool().must()
        q.isBool shouldBe true
        must.size shouldBe 2
        must.filter { it.isMatchBoolPrefix }.find { it.matchBoolPrefix().field() == "title" }?.matchBoolPrefix()?.query() shouldBe "quick brow"
        must.filter { it.isMatchBoolPrefix }.find { it.matchBoolPrefix().field() == "desc" }?.matchBoolPrefix()?.query() shouldBe "kotlin dsl"
    }

    test("must 쿼리에서 query가 null/빈 문자열이면 제외되어야함") {
        val q = query {
            boolQuery {
                mustQuery {
                    queries[
                        matchBoolPrefixQuery(field = "a", query = null),
                        matchBoolPrefixQuery(field = "b", query = ""),
                        matchBoolPrefixQuery(field = "c", query = "hello w")
                    ]
                }
            }
        }

        val must = q.bool().must()
        q.isBool shouldBe true
        must.size shouldBe 1
        must.first().matchBoolPrefix().field() shouldBe "c"
        must.first().matchBoolPrefix().query() shouldBe "hello w"
    }

    test("filter/mustNot/should 절에서도 동일하게 동작해야함") {
        val q = query {
            boolQuery {
                filterQuery {
                    queries[
                        matchBoolPrefixQuery(field = "a", query = "abc d")
                    ]
                }
                mustNotQuery {
                    queries[
                        matchBoolPrefixQuery(field = "b", query = "efg h")
                    ]
                }
                shouldQuery {
                    queries[
                        matchBoolPrefixQuery(field = "c", query = "ijk l")
                    ]
                }
            }
        }

        val filter = q.bool().filter()
        val mustNot = q.bool().mustNot()
        val should = q.bool().should()

        filter.size shouldBe 1
        mustNot.size shouldBe 1
        should.size shouldBe 1

        filter.first().matchBoolPrefix().field() shouldBe "a"
        mustNot.first().matchBoolPrefix().field() shouldBe "b"
        should.first().matchBoolPrefix().field() shouldBe "c"
    }

    test("옵션: operator, minimum_should_match, analyzer, boost, _name 적용 확인") {
        val q = query {
            boolQuery {
                mustQuery {
                    matchBoolPrefixQuery(
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

        val qb = q.bool().must().first().matchBoolPrefix()
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
