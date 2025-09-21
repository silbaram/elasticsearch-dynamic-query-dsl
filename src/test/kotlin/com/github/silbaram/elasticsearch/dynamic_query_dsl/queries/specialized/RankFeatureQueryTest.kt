package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldNotBeNull

class RankFeatureQueryTest : FunSpec({

    test("rank_feature saturation DSL builds with metadata") {
        val q = query {
            rankFeatureQuery {
                field = "pagerank"
                saturation { pivot = 2.5f }
                boost = 1.1f
                _name = "pagerank_rank"
            }
        }

        q.isRankFeature shouldBe true
        val rq = q.rankFeature()
        rq.field() shouldBe "pagerank"
        rq.boost() shouldBe 1.1f
        rq.queryName() shouldBe "pagerank_rank"
        rq.saturation()?.pivot() shouldBe 2.5f
        rq.log() shouldBe null
    }

    test("rank_feature linear function produces empty function payload") {
        val q = query {
            rankFeatureQuery {
                field = "freshness_score"
                linear()
            }
        }

        q.isRankFeature shouldBe true
        val rf = q.rankFeature()
        rf.linear().shouldNotBeNull()
        rf.saturation() shouldBe null
        rf.log() shouldBe null
        rf.sigmoid() shouldBe null
    }

    test("rank_feature logarithm requires scaling factor") {
        queryOrNull {
            rankFeatureQuery {
                field = "pagerank"
                logarithm { }
            }
        } shouldBe null

        val q = query {
            rankFeatureQuery {
                field = "pagerank"
                logarithm { scalingFactor = 3.5f }
            }
        }

        q.rankFeature().log()?.scalingFactor() shouldBe 3.5f
    }

    test("rank_feature sigmoid requires pivot and exponent") {
        queryOrNull {
            rankFeatureQuery {
                field = "signal"
                sigmoid { pivot = 3f }
            }
        } shouldBe null

        val q = query {
            rankFeatureQuery {
                field = "signal"
                sigmoid {
                    pivot = 3f
                    exponent = 1.2f
                }
            }
        }

        q.rankFeature().sigmoid()?.pivot() shouldBe 3f
        q.rankFeature().sigmoid()?.exponent() shouldBe 1.2f
    }

    test("rank_feature without function yields minimal query") {
        val q = query {
            rankFeatureQuery {
                field = "pagerank"
            }
        }

        q.isRankFeature shouldBe true
        val rf = q.rankFeature()
        rf.field() shouldBe "pagerank"
        rf.saturation() shouldBe null
        rf.log() shouldBe null
        rf.linear() shouldBe null
        rf.sigmoid() shouldBe null
    }
})
