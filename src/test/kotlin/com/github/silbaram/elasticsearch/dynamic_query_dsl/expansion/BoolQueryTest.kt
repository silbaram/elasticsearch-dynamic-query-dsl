package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion

import co.elastic.clients.elasticsearch._types.query_dsl.Query

class BoolQueryTest {


    fun query() {
        val boolQuery = Query.Builder().boolQuery {
            filter(listOf())
        }

        println(boolQuery.build())
    }
}

fun main() {
    BoolQueryTest().query()
}