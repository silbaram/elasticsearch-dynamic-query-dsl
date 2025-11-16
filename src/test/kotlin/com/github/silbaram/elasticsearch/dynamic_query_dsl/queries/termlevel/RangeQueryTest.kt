package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RangeQueryTest : FunSpec({

    test("must 쿼리에서 range 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    rangeQuery { field = "a"; from = "1234" }
                    rangeQuery { field = "a"; to = "5678" }
                    rangeQuery { field = "b"; gt = 123; lt = 567 }
                    rangeQuery { field = "c"; gte = 456; lte = 789 }
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 4

        mustQuery.filter { it.isRange && it.range().isUntyped && it.range().untyped().field() == "a"}.size shouldBe 2
        mustQuery.filter { it.isRange && it.range().isUntyped && it.range().untyped().field() == "b"}.size shouldBe 1
        mustQuery.filter { it.isRange && it.range().isUntyped && it.range().untyped().field() == "c"}.size shouldBe 1


        mustQuery.filter { it.isRange && it.range().isUntyped && it.range().untyped().field() == "a" && it.range().untyped().from() != null }[0]
            .toString().contains("\"from\":\"1234\"") shouldBe true
        mustQuery.filter { it.isRange && it.range().isUntyped && it.range().untyped().field() == "a" && it.range().untyped().to() != null }[0]
            .toString().contains("\"to\":\"5678\"") shouldBe true
        mustQuery.filter { it.isRange && it.range().isUntyped && it.range().untyped().field() == "b" }[0]
            .toString().contains("\"gt\":123") shouldBe true
        mustQuery.filter { it.isRange && it.range().isUntyped && it.range().untyped().field() == "b" }[0]
            .toString().contains("\"lt\":567") shouldBe true
        mustQuery.filter { it.isRange && it.range().isUntyped && it.range().untyped().field() == "c" }[0]
            .toString().contains("\"gte\":456") shouldBe true
        mustQuery.filter { it.isRange && it.range().isUntyped && it.range().untyped().field() == "c" }[0]
            .toString().contains("\"lte\":789") shouldBe true
    }

    test("must 쿼리에서 rangeQuery에 boost 설정시 적용이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    rangeQuery { field = "d"; gte = 10; lte = 20; boost = 3.0F }
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isRange && it.range().isUntyped }
            .find { it.range().untyped().field() == "d" }?.range()?.untyped()?.boost() shouldBe 3.0F
    }

    test("range 쿼리에 _name이 설정되면 range.queryName에 반영되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    rangeQuery { field = "d"; gte = 10; lte = 20; _name = "named" }
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isRange && it.range().isUntyped }
            .find { it.range().untyped().field() == "d" }!!.range().untyped().queryName() shouldBe "named"
    }
})
