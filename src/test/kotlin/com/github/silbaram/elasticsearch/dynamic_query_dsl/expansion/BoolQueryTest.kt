package com.github.silbaram.elasticsearch.dynamic_query_dsl.expansion

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BoolQueryTest: FunSpec({
    test("bool query 생성") {
        val boolQuery = Query.Builder().boolQuery {
            filterQuery (
                Query.Builder().term(TermQuery.Builder().field("field1").value("value1").build()).build()
            )
        }

        val boolQueryBuild = boolQuery.build()
        val filterQueryBuild = (boolQueryBuild._get() as BoolQuery).filter()
        val termQueryBuild = filterQueryBuild[0]

        boolQueryBuild._kind() shouldBe Query.Kind.Bool
        filterQueryBuild.size shouldBe 1
        termQueryBuild.isTerm shouldBe true
        termQueryBuild.term().field() shouldBe "field1"
        termQueryBuild.term().value().stringValue() shouldBe "value1"
    }
})