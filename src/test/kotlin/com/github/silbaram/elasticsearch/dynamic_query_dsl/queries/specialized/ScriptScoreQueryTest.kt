package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.matchAllDsl
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ScriptScoreQueryTest : FunSpec({

    test("script_score query - inline script with default match_all") {
        val q = query {
            scriptScoreQuery {
                inline(
                    source = "Math.log(2 + doc['rating'].value) * params.factor",
                    params = mapOf("factor" to 5)
                )
                boost = 2.0f
                minScore = 0.5f
                _name = "rating-score"
            }
        }

        q.shouldNotBeNull()
        q.isScriptScore shouldBe true
        val scriptScore = q.scriptScore()
        scriptScore.boost() shouldBe 2.0f
        scriptScore.queryName() shouldBe "rating-score"
        scriptScore.minScore() shouldBe 0.5f
        scriptScore.query().isMatchAll shouldBe true
        val inline = scriptScore.script().inline()
        inline.source() shouldBe "Math.log(2 + doc['rating'].value) * params.factor"
        inline.params()?.get("factor")?.toString() shouldBe "5"
    }

    test("script_score query - stored script with explicit inner query") {
        val q = query {
            scriptScoreQuery {
                query {
                    termQuery {
                        field = "status"
                        value = "active"
                    }
                }
                stored(id = "score-by-status", params = mapOf("weight" to 3))
                boost = 1.1f
            }
        }

        q.isScriptScore shouldBe true
        val scriptScore = q.scriptScore()
        scriptScore.boost() shouldBe 1.1f
        val stored = scriptScore.script().stored()
        stored.id() shouldBe "score-by-status"
        stored.params()?.get("weight")?.toString() shouldBe "3"
        val inner = scriptScore.query()
        inner.isTerm shouldBe true
        inner.term().field() shouldBe "status"
    }

    test("script_score query - usable within bool.must via helper") {
        val q = query {
            boolQuery {
                mustQuery {
                    scriptScoreQuery {
                        query {
                            matchAllDsl()
                        }
                        inline(source = "params.boost", params = mapOf("boost" to 2))
                    }
                }
            }
        }

        q.isBool shouldBe true
        val mustQueries = q.bool().must()
        mustQueries.size shouldBe 1
        val nested = mustQueries.first()
        nested.isScriptScore shouldBe true
        nested.scriptScore().script().inline().source() shouldBe "params.boost"
    }

    test("script_score query - invalid definitions are skipped") {
        val missingScript = queryOrNull {
            scriptScoreQuery {
                query {
                    matchAllDsl()
                }
            }
        }
        missingScript shouldBe null

        val blankStored = queryOrNull {
            scriptScoreQuery {
                stored(id = "   ")
            }
        }
        blankStored shouldBe null
    }
})
