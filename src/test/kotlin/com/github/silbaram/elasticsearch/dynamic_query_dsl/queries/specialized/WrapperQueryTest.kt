package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.nio.charset.StandardCharsets
import java.util.Base64

class WrapperQueryTest : FunSpec({

    test("wrapper query - raw json is encoded and metadata applied") {
        val rawJson = """{"match":{"status":"active"}}"""
        val q = query {
            wrapperQuery {
                rawJson(rawJson)
                boost = 1.2f
                _name = "raw-wrapper"
            }
        }

        q.shouldNotBeNull()
        q.isWrapper shouldBe true
        val wrapper = q.wrapper()
        wrapper.boost() shouldBe 1.2f
        wrapper.queryName() shouldBe "raw-wrapper"
        val decoded = String(Base64.getDecoder().decode(wrapper.query()), StandardCharsets.UTF_8)
        decoded shouldBe rawJson
    }

    test("wrapper query - accepts pre-encoded base64 payload") {
        val payload = "{\"term\":{\"user\":\"kimchy\"}}"
        val encoded = Base64.getEncoder().encodeToString(payload.toByteArray(StandardCharsets.UTF_8))
        val q = query {
            wrapperQuery {
                query = encoded
            }
        }

        q.isWrapper shouldBe true
        q.wrapper().query() shouldBe encoded
    }

    test("wrapper query - usable inside bool.must helper") {
        val rawJson = """{"range":{"age":{"gte":30}}}"""
        val q = query {
            boolQuery {
                mustQuery {
                    wrapperQuery {
                        rawJson(rawJson)
                    }
                }
            }
        }

        q.isBool shouldBe true
        val mustQueries = q.bool().must()
        mustQueries.size shouldBe 1
        val nested = mustQueries.first()
        nested.isWrapper shouldBe true
        val decoded = String(Base64.getDecoder().decode(nested.wrapper().query()), StandardCharsets.UTF_8)
        decoded shouldBe rawJson
    }

    test("wrapper query - invalid definitions are skipped") {
        val blankEncoded = queryOrNull {
            wrapperQuery {
                query = "  "
            }
        }
        blankEncoded shouldBe null

        val emptyRaw = queryOrNull {
            wrapperQuery {
                rawJson(" ")
            }
        }
        emptyRaw shouldBe null
    }
})
