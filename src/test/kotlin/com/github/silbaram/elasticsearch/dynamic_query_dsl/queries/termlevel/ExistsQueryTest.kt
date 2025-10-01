package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ExistsQueryTest : FunSpec({

    test("must 쿼리에서 exists 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    existsQuery { field = "a" }
                }
            }
        }
        val mustQuery = query.bool().must()
        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.first().isExists shouldBe true
        mustQuery.first().exists().field() shouldBe "a"
    }

    test("must 쿼리에서 exists 쿼리 생성시 field가 null이면 existsQuery가 빠져서 생성되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    existsQuery { field = null }
                    termQuery { field = "a"; value = "1111" }
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.first().isTerm shouldBe true
        mustQuery.first().term().field() shouldBe "a"
    }

    test("exists 쿼리에 boost 설정시 적용이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    existsQuery { field = "a"; boost = 2.0F }
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isExists }.find { it.exists().field() == "a" }?.exists()?.field() shouldBe "a"
        mustQuery.filter { it.isExists }.find { it.exists().field() == "a" }?.exists()?.boost() shouldBe 2.0F
    }

    test("exists 쿼리에 _name이 설정되면 terms.queryName에 반영되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    existsQuery { field = "a"; _name = "named" }
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isExists }.find { it.exists().field() == "a" }?.exists()?.field() shouldBe "a"
        mustQuery.filter { it.isExists }.find { it.exists().field() == "a" }?.exists()?.queryName() shouldBe "named"
    }
})
