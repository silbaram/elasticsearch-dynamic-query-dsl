package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ScriptQueryTest : FunSpec({

    test("script query - inline script with params and metadata") {
        val q = query {
            scriptQuery {
                inline(
                    source = "doc['score'].value > params.threshold",
                    lang = "painless",
                    params = mapOf(
                        "threshold" to 10,
                        "flag" to true
                    )
                )
                boost = 1.5f
                _name = "inline-script"
            }
        }

        q.shouldNotBeNull()
        q.isScript shouldBe true
        val scriptQuery = q.script()
        scriptQuery.boost() shouldBe 1.5f
        scriptQuery.queryName() shouldBe "inline-script"
        val inline = scriptQuery.script()
        inline.source() shouldBe "doc['score'].value > params.threshold"
        inline.lang() shouldBe "painless"
        inline.params()?.size shouldBe 2
        inline.params()?.containsKey("threshold") shouldBe true
        inline.params()?.containsKey("flag") shouldBe true
        q.toString().contains("\"inline-script\"") shouldBe true
    }

    test("script query - stored script uses id and params") {
        val q = query {
            scriptQuery {
                stored(id = "stored-script-1", params = mapOf("userId" to "kimchy"))
                boost = 0.7f
            }
        }

        q.isScript shouldBe true
        val scriptQuery = q.script()
        scriptQuery.boost() shouldBe 0.7f
        val stored = scriptQuery.script()
        stored.id() shouldBe "stored-script-1"
        stored.params()?.containsKey("userId") shouldBe true
    }

    test("script query - usable inside bool helper") {
        val q = query {
            boolQuery {
                mustQuery {
                    scriptQuery {
                        inline(
                            source = "doc['votes'].value >= params.min"
                        )
                        params("min" to 42)
                    }
                }
            }
        }

        q.isBool shouldBe true
        val must = q.bool().must()
        must.size shouldBe 1
        val nested = must.first()
        nested.isScript shouldBe true
        nested.script().script().source() shouldBe "doc['votes'].value >= params.min"
    }

    test("script query - invalid definitions are ignored") {
        val missingContent = queryOrNull {
            scriptQuery {
                // only lang, no id/source
                lang = "painless"
            }
        }
        missingContent shouldBe null

        val blankSource = queryOrNull {
            scriptQuery {
                source = "   "
                params = mapOf("threshold" to 1)
            }
        }
        blankSource shouldBe null

        val blankId = queryOrNull {
            scriptQuery {
                stored(id = " ")
            }
        }
        blankId shouldBe null
    }
})
