package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class RuleQueryTest : FunSpec({

    test("rule query - builds with organic fallback and match criteria") {
        val criteria = mapOf(
            "field_categories" to mapOf(
                "country" to listOf("US", "KR")
            ),
            "metadata" to mapOf("channel" to "web")
        )

        val q = query {
            ruleQueryDsl {
                rulesetIds("rule-1", " rule-2 ")
                organic {
                    matchQuery {
                        field = "title"
                        query = "elastic"
                    }
                }
                matchCriteria(criteria)
                boost = 1.4f
                _name = "rule-sample"
            }
        }

        q.shouldNotBeNull()
        q.isRule shouldBe true
        val rule = q.rule()
        rule.boost() shouldBe 1.4f
        rule.queryName() shouldBe "rule-sample"
        rule.rulesetIds() shouldBe listOf("rule-1", "rule-2")
        rule.organic().isMatch shouldBe true
        rule.matchCriteria().toString().contains("field_categories") shouldBe true
    }

    test("rule query - usable inside bool.must helper") {
        val q = query {
            boolQuery {
                mustQuery {
                    ruleQuery {
                        rulesetIds(listOf(" featured "))
                        organic {
                            matchQuery {
                                field = "status"
                                query = "active"
                            }
                        }
                        matchCriteria(mapOf("tags" to listOf("kotlin")))
                    }
                }
            }
        }

        q.isBool shouldBe true
        val mustQueries = q.bool().must()
        mustQueries.size shouldBe 1
        val nested = mustQueries.first()
        nested.isRule shouldBe true
        nested.rule().rulesetIds() shouldBe listOf("featured")
    }

    test("rule query - invalid definitions are skipped") {
        val missingIds = queryOrNull {
            ruleQueryDsl {
                organic {
                    matchQuery {
                        field = "title"
                        query = "dsl"
                    }
                }
                matchCriteria(mapOf("category" to "tech"))
            }
        }
        missingIds shouldBe null

        val missingOrganic = queryOrNull {
            ruleQueryDsl {
                rulesetIds("rule-1")
                matchCriteria(mapOf("category" to "tech"))
            }
        }
        missingOrganic shouldBe null

        val missingCriteria = queryOrNull {
            ruleQueryDsl {
                rulesetIds("rule-1")
                organic {
                    matchQuery {
                        field = "title"
                        query = "dsl"
                    }
                }
            }
        }
        missingCriteria shouldBe null
    }
})
