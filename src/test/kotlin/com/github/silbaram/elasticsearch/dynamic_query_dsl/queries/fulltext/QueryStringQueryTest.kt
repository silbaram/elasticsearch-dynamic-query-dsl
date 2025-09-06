package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class QueryStringQueryTest : FunSpec({

    test("최상위 query_string 쿼리 생성") {
        val q = query {
            queryString(
                query = "title:(kotlin AND coroutine) AND body:tips",
                fields = listOf("title^2", "body"),
                defaultOperator = Operator.And,
                analyzeWildcard = true,
                allowLeadingWildcard = false,
                minimumShouldMatch = "2"
            )
        }

        q.isQueryString shouldBe true
        val qs = q.queryString()
        qs.query() shouldBe "title:(kotlin AND coroutine) AND body:tips"
        qs.fields().size shouldBe 2
        qs.defaultOperator() shouldBe Operator.And
        qs.analyzeWildcard() shouldBe true
        qs.allowLeadingWildcard() shouldBe false
        qs.minimumShouldMatch() shouldBe "2"
    }

    test("must 절에서 query_string 생성/생략 동작") {
        val q = query {
            boolQuery {
                mustQuery {
                    queries[
                        queryStringQuery("kotlin* AND \"structured query\"", listOf("title", "body")),
                        queryStringQuery(null),
                        queryStringQuery("")
                    ]
                }
            }
        }

        val must = q.bool().must()
        must.size shouldBe 1
        val qs = must.first().queryString()
        qs.query() shouldBe "kotlin* AND \"structured query\""
        qs.fields().size shouldBe 2
    }

    test("추가 옵션 적용: analyzer/quoteAnalyzer/phraseSlop/fuzziness/lenient") {
        val q = query {
            queryString(
                query = "name:jon~ AND (city:\"New York\"~2)",
                analyzer = "standard",
                quoteAnalyzer = "standard",
                phraseSlop = 2.0,
                fuzziness = "AUTO",
                lenient = true,
                quoteFieldSuffix = ".exact"
            )
        }

        val qs = q.queryString()
        qs.analyzer() shouldBe "standard"
        qs.quoteAnalyzer() shouldBe "standard"
        qs.phraseSlop() shouldBe 2.0
        qs.fuzziness() shouldBe "AUTO"
        qs.lenient() shouldBe true
        qs.quoteFieldSuffix() shouldBe ".exact"
    }
})
