package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MatchAllQueryTest: FunSpec ({

    test("must 쿼리에서 match_all 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchAllQuery()
                }
            }
        }
        
        val mustQuery = query.bool().must()
        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.first().isMatchAll shouldBe true
    }

    test("match_all 쿼리를 단독으로 생성할 수 있어야함") {
        val matchQuery = matchAllQuery()
        
        matchQuery.isMatchAll shouldBe true
    }

    test("match_all 쿼리에 boost 설정시 적용이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchAllQuery(boost = 2.0F)
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.first().isMatchAll shouldBe true
        mustQuery.first().matchAll().boost() shouldBe 2.0F
    }

    test("match_all 쿼리에 _name이 설정되면 queryName에 반영되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchAllQuery(_name = "match_all_named")
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.first().isMatchAll shouldBe true
        mustQuery.first().matchAll().queryName() shouldBe "match_all_named"
    }

    test("match_all 쿼리에 boost와 _name을 모두 설정할 수 있어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchAllQuery(boost = 1.5F, _name = "boosted_match_all")
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.first().isMatchAll shouldBe true
        mustQuery.first().matchAll().boost() shouldBe 1.5F
        mustQuery.first().matchAll().queryName() shouldBe "boosted_match_all"
    }

    test("match_all 쿼리를 queries 배열에서 사용할 수 있어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        matchAllQuery(boost = 1.2F),
                        termQuery(field = "category", value = "tech")
                    ]
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 2
        mustQuery.find { it.isMatchAll }?.matchAll()?.boost() shouldBe 1.2F
        mustQuery.find { it.isTerm }?.term()?.field() shouldBe "category"
    }

    test("match_all 쿼리 JSON 직렬화가 올바르게 되어야함") {
        val query = matchAllQuery()
        
        // 기본 match_all 쿼리는 빈 객체를 가져야 함
        query.isMatchAll shouldBe true
        
        // boost가 있는 경우도 테스트
        val queryWithBoost = matchAllQuery(boost = 1.5F, _name = "test_match_all")
        queryWithBoost.isMatchAll shouldBe true
        queryWithBoost.matchAll().boost() shouldBe 1.5F
        queryWithBoost.matchAll().queryName() shouldBe "test_match_all"
    }
})