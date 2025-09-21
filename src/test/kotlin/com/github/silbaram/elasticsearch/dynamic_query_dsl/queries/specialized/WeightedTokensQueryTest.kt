package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class WeightedTokensQueryTest : FunSpec({

    test("weighted tokens query - builds with tokens and pruning config") {
        val q = query {
            weightedTokensQuery {
                field = "title.embedding"
                tokens(
                    "kotlin" to 1.0,
                    "dsl" to 0.7,
                    "search" to 0.4
                )
                pruningConfig {
                    tokensFreqRatioThreshold = 3
                    tokensWeightThreshold = 0.2f
                    onlyScorePrunedTokens = true
                }
                boost = 1.1f
                _name = "weighted-sample"
            }
        }

        q.shouldNotBeNull()
        q.isWeightedTokens shouldBe true
        val weighted = q.weightedTokens()
        weighted.boost() shouldBe 1.1f
        weighted.queryName() shouldBe "weighted-sample"
        weighted.field() shouldBe "title.embedding"
        weighted.tokens()["kotlin"] shouldBe 1.0f
        weighted.tokens().size shouldBe 3
        val pruning = weighted.pruningConfig()
        pruning.shouldNotBeNull()
        pruning.tokensFreqRatioThreshold() shouldBe 3
        pruning.tokensWeightThreshold() shouldBe 0.2f
        pruning.onlyScorePrunedTokens() shouldBe true
    }

    test("weighted tokens query - usable inside bool.must helper") {
        val q = query {
            boolQuery {
                mustQuery {
                    weightedTokensQuery {
                        field = "title.embedding"
                        tokens("elastic" to 1.5)
                    }
                }
            }
        }

        q.isBool shouldBe true
        val must = q.bool().must()
        must.size shouldBe 1
        val nested = must.first()
        nested.isWeightedTokens shouldBe true
        nested.weightedTokens().tokens()["elastic"] shouldBe 1.5f
    }

    test("weighted tokens query - invalid inputs are skipped") {
        val missingField = queryOrNull {
            weightedTokensQuery {
                tokens("kotlin" to 1.0)
            }
        }
        missingField shouldBe null

        val missingTokens = queryOrNull {
            weightedTokensQuery {
                field = "title.embedding"
            }
        }
        missingTokens shouldBe null

        val emptyTokens = queryOrNull {
            weightedTokensQuery {
                field = "title.embedding"
                tokens(emptyMap())
            }
        }
        emptyTokens shouldBe null
    }
})
