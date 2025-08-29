package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries

import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.shouldQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FunctionScoreQueryTest : FunSpec({

    test("function_score가 bool query를 감싸고 함수가 적용되어야함") {
        val query = Query.Builder().functionScoreQuery {
            query {
                boolQuery {
                    mustQuery {
                        termQuery(
                            field = "description",
                            value = "강남역"
                        )
                    }
                    filterQuery {
                        termQuery(
                            field = "is_open",
                            value = true
                        )
                    }
                }
            }
            functions {
                function {
                    fieldValueFactor {
                        field("rating")
                        factor(1.5)
                    }
                }
                function {
                    randomScore { }
                }
            }
            scoreMode = FunctionScoreMode.Sum
            boostMode = FunctionBoostMode.Multiply
        }.build()

        query.isFunctionScore shouldBe true
        query.functionScore().query()?.isBool shouldBe true
        query.functionScore().functions().size shouldBe 2
        query.functionScore().scoreMode() shouldBe FunctionScoreMode.Sum
        query.functionScore().boostMode() shouldBe FunctionBoostMode.Multiply
    }

    test("function_score가 multi_match query를 감싸고 field_value_factor가 적용되어야함") {
        val query = Query.Builder().functionScoreQuery {
            query {
                Query.of { q ->
                    q.multiMatch { mm ->
                        mm.query("친환경 세제")
                        mm.fields("name", "description")
                    }
                }
            }
            functions {
                function {
                    fieldValueFactor {
                        field("eco_score")
                    }
                }
            }
        }.build()

        query.isFunctionScore shouldBe true
        query.functionScore().query()?.isMultiMatch shouldBe true
        query.functionScore().functions().size shouldBe 1
    }

    test("bool query의 should에 function_score가 포함되어야함") {
        val query = Query.Builder().boolQuery {
            mustQuery {
                termQuery(
                    field = "position",
                    value = "백엔드 개발자"
                )
            }
            shouldQuery {
                Query.Builder().apply {
                    functionScoreQuery {
                        query {
                            Query.of { it.matchAll { } }
                        }
                        functions {
                            function {
                                scriptScore {
                                    script { s ->
                                        s.inline { i -> i.source("1") }
                                    }
                                }
                            }
                        }
                        boostMode = FunctionBoostMode.Replace
                    }
                }.build()
            }
        }.build()

        query.isBool shouldBe true
        query.bool().must().size shouldBe 1
        query.bool().should().size shouldBe 1
        query.bool().should()[0].isFunctionScore shouldBe true
        query.bool().should()[0].functionScore().boostMode() shouldBe FunctionBoostMode.Replace
    }

    test("dis_max query에 function_score가 쿼리 중 하나로 포함되어야함") {
        val functionScoreQuery = Query.Builder().apply {
            functionScoreQuery {
                functions {
                    function {
                        scriptScore {
                            script { s ->
                                s.inline { i -> i.source("doc['likes'].value * 0.2 + doc['views'].value * 0.1") }
                            }
                        }
                    }
                }
            }
        }.build()

        val disMaxQuery = Query.of { q ->
            q.disMax { dm ->
                dm.queries(
                    Query.of { it.match { m -> m.field("title").query("Elasticsearch").boost(2.0f) } },
                    Query.of { it.match { m -> m.field("content").query("Elasticsearch") } },
                    functionScoreQuery
                )
            }
        }

        disMaxQuery.isDisMax shouldBe true
        disMaxQuery.disMax().queries().size shouldBe 3
        disMaxQuery.disMax().queries()[2].isFunctionScore shouldBe true
    }
})
