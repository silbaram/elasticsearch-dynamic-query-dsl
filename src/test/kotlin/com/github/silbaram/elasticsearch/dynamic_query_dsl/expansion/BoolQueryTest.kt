package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery
import io.kotest.core.spec.style.FunSpec

class BoolQueryTest: FunSpec({
    test("bool query 생성") {
        val boolQuery = Query.Builder().boolQuery {
//            filterQuery (
//                Query.Builder().term(TermQuery.Builder().field("field1").value("value1").build()).build(),
//                Query.Builder().term(TermQuery.Builder().field("field4").value("value4").build()).build()
//            )

            filterQuery {
                listOf(Query.Builder().term(TermQuery.Builder().field("field1").value("value1").build()))
            }
        }
        println(boolQuery.build().toString())
//        val query = boolQuery.build()
//        val tempQuery = "Query: {\"bool\":{\"filter\":[]}}"
//
//        query.toString() shouldBe tempQuery
    }
})