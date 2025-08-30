package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.helper.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class FunctionScoreQueryTest : FunSpec({

    test("function_score: 단일 쿼리 + 단일 함수") {
        val functionScore = query {
            functionScoreQuery {
                query {
                    termQuery(field = "status", value = "active")
                }
                function {
                    fieldValueFactorQuery {
                        field("rating")
                        modifier("ln2p")
                        factor(1.5)
                    }
                }
                scoreMode = FunctionScoreMode.Sum
                boostMode = FunctionBoostMode.Multiply
            }
        }
        println("functionScore = $functionScore")
        functionScore.isFunctionScore shouldBe true
        functionScore.functionScore().query()!!.isTerm shouldBe true
        functionScore.functionScore().functions().size shouldBe 1
        functionScore.functionScore().functions()[0].fieldValueFactor().field() shouldBe "rating"
        functionScore.functionScore().scoreMode() shouldBe FunctionScoreMode.Sum
        functionScore.functionScore().boostMode() shouldBe FunctionBoostMode.Multiply
    }

    test("function_score: 여러 쿼리 + 다중 함수") {
        val functionScore = query {
            functionScoreQuery {
                query {
                    queries[
                        termQuery(field = "f1", value = "v1"),
                        termQuery(field = "f2", value = "v2"),
                    ]
                }
                function {
                    fieldValueFactorQuery { field("a") }
                }
                function {
                    fieldValueFactorQuery { field("b") }
                }
            }
        }
        println("functionScore = $functionScore")
        functionScore.isFunctionScore shouldBe true
        functionScore.functionScore().query()!!.isBool shouldBe true
        functionScore.functionScore().query()!!.bool().must().size shouldBe 2
        functionScore.functionScore().functions().size shouldBe 2
    }

    test("function_score가 bool 쿼리를 감싸고 다중 함수로 점수 합산/곱 적용") {
        val q = query {
            functionScoreQuery {
                // bool 내부에 must 2개, filter 1개
                query {
                    boolQuery {
                        mustQuery {
                            queries[
                                termQuery(field = "description", value = "강남역"),
                                termQuery(field = "description", value = "파스타")
                            ]
                        }
                        filterQuery {
                            termQuery(field = "is_open", value = "true")
                        }
                    }
                }
                // 평점, 리뷰수에 기반한 가중치 함수들
                function {
                    this.fieldValueFactor { f ->
                        f.field("rating"); f.modifier(FieldValueFactorModifier.Ln2p); f.factor(1.5)
                    }
                }
                function {
                    this.fieldValueFactor { f ->
                        f.field("review_count"); f.modifier(FieldValueFactorModifier.Log1p)
                    }
                }
                // score_mode, boost_mode
                scoreMode = FunctionScoreMode.Sum
                boostMode = FunctionBoostMode.Multiply
            }
        }

        q.isFunctionScore shouldBe true
        q.functionScore().query()!!.isBool shouldBe true
        q.functionScore().query()!!.bool().must().size shouldBe 2
        q.functionScore().functions().size shouldBe 2
        q.functionScore().scoreMode() shouldBe FunctionScoreMode.Sum
        q.functionScore().boostMode() shouldBe FunctionBoostMode.Multiply
    }

    test("function_score가 multi_match 결과에 eco_score 가산점 부여") {
        val q = query {
            functionScoreQuery {
                // multi_match 기반 검색
                query {
                    Query.of { qb ->
                        qb.multiMatch { mm ->
                            mm.query("친환경 세제")
                                .fields("name", "description")
                        }
                    }
                }
                // eco_score가 높을수록 가산점
                function {
                    this.fieldValueFactor { f ->
                        f.field("eco_score"); f.modifier(FieldValueFactorModifier.Sqrt)
                    }
                }
            }
        }

        q.isFunctionScore shouldBe true
        q.functionScore().query()!!.isMultiMatch shouldBe true
        q.functionScore().functions().size shouldBe 1
    }

    test("bool 쿼리의 should 절에 function_score 포함 (boost_mode=replace)") {
        val q = query {
            boolQuery {
                mustQuery {
                    termQuery(field = "position", value = "백엔드 개발자")
                }
                shouldQuery {
                    this.queries[
                        // 단순 태그 가산점 (terms 대신 term으로 간략화)
                        termQuery(field = "company_tags", value = "네카라쿠배"),
                        // function_score 자체를 하나의 조건으로 사용
                        Query.of { qb ->
                            qb.functionScore { fs ->
                                fs.query(Query.of { qq -> qq.matchAll { ma -> ma } })
                                fs.functions { f -> f.fieldValueFactor { ff -> ff.field("freshness_score") } }
                                fs.boostMode(FunctionBoostMode.Replace)
                            }
                        }
                    ]
                }
            }
        }

        q.isBool shouldBe true
        val bq = q.bool()
        bq.must().size shouldBe 1
        bq.should().size shouldBe 2
        bq.should().any { it.isFunctionScore } shouldBe true
    }

    test("dis_max가 title/content와 function_score 인기도를 결합") {
        val disMaxQuery = query {
            disMax { d ->
                val q1 = Query.of { it.match { m -> m.field("title").query("Elasticsearch").boost(2.0f) } }
                val q2 = Query.of { it.match { m -> m.field("content").query("Elasticsearch") } }
                val q3 = Query.of { qb ->
                    qb.functionScore { fs ->
                        // 인기도 점수: likes, views 기반(간략화: 두 개의 field_value_factor)
                        fs.functions { f -> f.fieldValueFactor { ff -> ff.field("likes").factor(0.2) } }
                        fs.functions { f -> f.fieldValueFactor { ff -> ff.field("views").factor(0.1) } }
                    }
                }
                d.queries(q1, q2, q3)
            }
        }

        disMaxQuery.isDisMax shouldBe true
        disMaxQuery.disMax().queries().size shouldBe 3
        disMaxQuery.disMax().queries().any { it.isFunctionScore } shouldBe true
    }

    test("function_score: filter + weight 가중치 적용") {
        val q = query {
            functionScoreQuery {
                query {
                    termQuery(field = "message", value = "elasticsearch")
                }
                function {
                    // is_premium=true 인 문서에 weight 2 적용
                    this.filter(Query.of { it.term { t -> t.field("is_premium").value(true) } })
                    this.weight(2.0)
                }
                boostMode = FunctionBoostMode.Multiply
            }
        }

        q.isFunctionScore shouldBe true
        val fsq = q.functionScore()
        fsq.functions().size shouldBe 1
        val f = fsq.functions()[0]
        f.weight() shouldBe 2.0
        f.filter()!!.isTerm shouldBe true
        fsq.boostMode() shouldBe FunctionBoostMode.Multiply
    }

    test("function_score: random_score로 랜덤 정렬 (boost_mode=replace)") {
        val q = query {
            functionScoreQuery {
                query { Query.of { it.matchAll { ma -> ma } } }
                function {
                    // 랜덤 점수 사용 (seed 생략)
                    this.randomScore { r -> r }
                }
                boostMode = FunctionBoostMode.Replace
            }
        }

        q.isFunctionScore shouldBe true
        val fsq = q.functionScore()
        fsq.functions().size shouldBe 1
        fsq.functions()[0].randomScore() shouldNotBe null
        fsq.boostMode() shouldBe FunctionBoostMode.Replace
    }
})
