package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

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
                    spanFirstQuery {
                        match = spanTermQuery("user.id", "kimchy")
                        end = 3
                        boost = 1.2f
                        _name = "my_query"
                    }
                }
            }
        }

        val expected = """Query: {"bool":{"must":[{"span_first":{"boost":1.2000000476837158,"_name":"my_query","end":3,"match":{"span_term":{"user.id":{"value":"kimchy"}}}}}]}}"""

        query.shouldNotBeNull()
        query.toString() shouldBe expected
    }

    test("spanFirstQuery should return null if match is not set") {
        val query = spanFirstQuery {
            end = 3
        }
        query shouldBe null
    }

    test("spanFirstQuery should throw exception if end is not set") {
        shouldThrow<IllegalArgumentException> {
            spanFirstQuery {
                match = spanTermQuery("user.id", "kimchy")
            }
        }
    }

    test("Simple spanFirstQuery usage") {
        val dsl = spanFirstQuery {
            match = spanTermQuery(field = "user.id", value = "kimchy")
            end = 3
        }

        val expected = """Query: {"span_first":{"end":3,"match":{"span_term":{"user.id":{"value":"kimchy"}}}}}"""

        dsl.shouldNotBeNull()
        dsl.toString() shouldBe expected
    }
})