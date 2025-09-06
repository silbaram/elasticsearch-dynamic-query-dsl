package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.SimpleQueryStringFlag
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SimpleQueryStringQueryTest : FunSpec({

    test("최상위 simple_query_string 생성") {
        val q = query {
            simpleQueryString(
                query = "kotlin +coroutine | \"structured query\"",
                fields = listOf("title^2", "body"),
                defaultOperator = Operator.Or,
                minimumShouldMatch = "2",
                analyzer = "standard",
                quoteFieldSuffix = ".exact",
                analyzeWildcard = true,
                flags = listOf(SimpleQueryStringFlag.Prefix, SimpleQueryStringFlag.Phrase),
                fuzzyMaxExpansions = 50,
                fuzzyPrefixLength = 1,
                fuzzyTranspositions = true,
                autoGenerateSynonymsPhraseQuery = true,
                lenient = true
            )
        }

        q.isSimpleQueryString shouldBe true
        val sq = q.simpleQueryString()
        sq.query() shouldBe "kotlin +coroutine | \"structured query\""
        sq.fields().size shouldBe 2
        sq.defaultOperator() shouldBe Operator.Or
        sq.minimumShouldMatch() shouldBe "2"
        sq.analyzer() shouldBe "standard"
        sq.quoteFieldSuffix() shouldBe ".exact"
        sq.analyzeWildcard() shouldBe true
        sq.flags().contains(SimpleQueryStringFlag.Prefix) shouldBe true
        sq.flags().contains(SimpleQueryStringFlag.Phrase) shouldBe true
        sq.fuzzyMaxExpansions() shouldBe 50
        sq.fuzzyPrefixLength() shouldBe 1
        sq.fuzzyTranspositions() shouldBe true
        sq.autoGenerateSynonymsPhraseQuery() shouldBe true
        sq.lenient() shouldBe true
    }

    test("must 절에서 simple_query_string 생성/생략") {
        val q = query {
            boolQuery {
                mustQuery {
                    queries[
                        simpleQueryStringQuery("kotlin* AND tags:(dsl | search)", listOf("title", "tags")),
                        simpleQueryStringQuery(null),
                        simpleQueryStringQuery("")
                    ]
                }
            }
        }

        val must = q.bool().must()
        must.size shouldBe 1
        val sq = must.first().simpleQueryString()
        sq.query() shouldBe "kotlin* AND tags:(dsl | search)"
        sq.fields().size shouldBe 2
    }
})
