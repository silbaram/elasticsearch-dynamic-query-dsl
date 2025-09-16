package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SpanFirstQueryTest : FunSpec({
    test("spanFirstQuery with lambda should build a valid query") {
        val query = query {
            boolQuery {
                mustQuery {
                    query {
                        spanFirstQueryDsl {
                            match { spanTermQuery { field = "user.id"; value = "kimchy" } }
                            end = 3
                            boost = 1.2f
                            _name = "my_query"
                        }
                    }
                }
            }
        }

        val expected = """Query: {"bool":{"must":[{"span_first":{"boost":1.2000000476837158,"_name":"my_query","end":3,"match":{"span_term":{"user.id":{"value":"kimchy"}}}}}]}}"""

        query.shouldNotBeNull()
        query.toString() shouldBe expected
    }

    test("spanFirstQuery should return null if match is not set") {
        val query = com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull {
            spanFirstQueryDsl {
                end = 3
            }
        }
        query shouldBe null
    }

    test("spanFirstQuery should throw exception if end is not set") {
        val q = com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull {
            spanFirstQueryDsl { match { spanTermQuery { field = "user.id"; value = "kimchy" } } }
        }
        q shouldBe null
    }

    test("Simple spanFirstQuery usage") {
        val dsl = query {
            spanFirstQueryDsl {
                match { spanTermQuery { field = "user.id"; value = "kimchy" } }
                end = 3
            }
        }

        dsl.shouldNotBeNull()
        dsl.isSpanFirst shouldBe true
    }
})
