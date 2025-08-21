package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.item_level

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery
import co.elastic.clients.json.JsonData
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.bool_clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion.compound_queries.boolQuery
import io.kotest.assertions.print.print
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private fun RangeQuery.fieldName(): String? {
    val method = this.javaClass.methods.firstOrNull { it.name == "field" && it.parameterCount == 0 }
    return if (method != null) {
        method.invoke(this) as? String
    } else {
        val field = this.javaClass.getDeclaredField("field")
        field.isAccessible = true
        field.get(this) as? String
    }
}

private fun RangeQuery.jsonValue(name: String): JsonData? {
    val method = this.javaClass.methods.firstOrNull { it.name == name && it.parameterCount == 0 }
    val value = if (method != null) {
        method.invoke(this)
    } else {
        val field = this.javaClass.declaredFields.firstOrNull { it.name == name } ?: return null
        field.isAccessible = true
        field.get(this)
    }

    return when (value) {
        null -> null
        is JsonData -> value
        else -> JsonData.of(value)
    }
}

class RangeQueryTest: FunSpec ({

    test("must 쿼리에서 range 쿼리 생성이 되어야함") {
        val boolQuery = Query.Builder()
            .boolQuery {
                mustQuery(
                    rangeQuery(
                        field = "a",
                        from = "1234"
                    ),
                    rangeQuery(
                        field = "a",
                        to = "5678"
                    ),
                    rangeQuery(
                        field = "b",
                        gt = 123,
                        lt = 567
                    ),
                    rangeQuery(
                        field = "c",
                        gte = 456,
                        lte = 789
                    )
                )
            }

        val boolQueryBuild = boolQuery.build()
        val mustQuery = boolQueryBuild.bool().must()

        boolQueryBuild.isBool shouldBe true
        mustQuery.size shouldBe 4

        mustQuery.filter { it.isRange && it.range().fieldName() == "a"}.size shouldBe 2
        mustQuery.filter { it.isRange && it.range().fieldName() == "b"}.size shouldBe 1
        mustQuery.filter { it.isRange && it.range().fieldName() == "c"}.size shouldBe 1

        mustQuery.filter { it.isRange && it.range().fieldName() == "a" && it.range().jsonValue("from") != null }[0]
            .range().jsonValue("from")!!.print().value shouldBe "\"1234\""
        mustQuery.filter { it.isRange && it.range().fieldName() == "a" && it.range().jsonValue("to") != null }[0]
            .range().jsonValue("to")!!.print().value shouldBe "\"5678\""
        mustQuery.filter { it.isRange && it.range().fieldName() == "b" }[0]
            .range().jsonValue("gt")!!.print().value shouldBe "123"
        mustQuery.filter { it.isRange && it.range().fieldName() == "b" }[0]
            .range().jsonValue("lt")!!.print().value shouldBe "567"
        mustQuery.filter { it.isRange && it.range().fieldName() == "c" }[0]
            .range().jsonValue("gte")!!.print().value shouldBe "456"
        mustQuery.filter { it.isRange && it.range().fieldName() == "c" }[0]
            .range().jsonValue("lte")!!.print().value shouldBe "789"
    }
})