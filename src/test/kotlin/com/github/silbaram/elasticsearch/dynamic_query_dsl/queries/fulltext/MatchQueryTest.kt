package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MatchQueryTest : FunSpec({

    test("must 쿼리에서 match 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchQuery { field = "a"; query = "1111" }
                    matchQuery { field = "b"; query = "2222" }
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 2
        mustQuery.filter { it.isMatch }.find { it.match().field() == "a" }?.match()?.query()?.stringValue() shouldBe "1111"
        mustQuery.filter { it.isMatch }.find { it.match().field() == "b" }?.match()?.query()?.stringValue() shouldBe "2222"
    }

    test("mustQuery DSL은 queries 블록 없이도 여러 match 쿼리를 추가할 수 있어야 함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchQuery { field = "title"; query = "kotlin" }
                    matchQuery { field = "body"; query = "dsl" }
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 2
        mustQuery.any { it.match().field() == "title" && it.match().query().stringValue() == "kotlin" } shouldBe true
        mustQuery.any { it.match().field() == "body" && it.match().query().stringValue() == "dsl" } shouldBe true
    }

    test("must 쿼리에서 matchQuery에 query 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchQuery { field = "a"; query = null }
                    matchQuery { field = "b"; query = "" }
                    matchQuery { field = "c"; query = "3333" }
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isMatch }.find { it.match().field() == "a" } shouldBe null
        mustQuery.filter { it.isMatch }.find { it.match().field() == "b" } shouldBe null
        mustQuery.filter { it.isMatch }.find { it.match().field() == "c" }?.match()?.query()?.stringValue() shouldBe "3333"
    }

    test("must 쿼리에서 matchQuery가 없을때 must쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchQuery { field = "a"; query = "" }
                    matchQuery { field = "b"; query = null }
                }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 0
    }

    test("filter 쿼리에서 match 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                filterQuery {
                    matchQuery { field = "a"; query = "1111" }
                    matchQuery { field = "b"; query = "2222" }
                }
            }
        }

        val filterQuery = query.bool().filter()

        query.isBool shouldBe true
        filterQuery.size shouldBe 2
        filterQuery.filter { it.isMatch }.find { it.match().field() == "a" }?.match()?.query()?.stringValue() shouldBe "1111"
        filterQuery.filter { it.isMatch }.find { it.match().field() == "b" }?.match()?.query()?.stringValue() shouldBe "2222"
    }

    test("filter 쿼리에서 matchQuery에 query 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                filterQuery {
                    matchQuery { field = "a"; query = null }
                    matchQuery { field = "b"; query = "" }
                    matchQuery { field = "c"; query = "3333" }
                }
            }
        }

        val filterQuery = query.bool().filter()

        query.isBool shouldBe true
        filterQuery.size shouldBe 1
        filterQuery.filter { it.isMatch }.find { it.match().field() == "a" } shouldBe null
        filterQuery.filter { it.isMatch }.find { it.match().field() == "b" } shouldBe null
        filterQuery.filter { it.isMatch }.find { it.match().field() == "c" }?.match()?.query()?.stringValue() shouldBe "3333"
    }

    test("filter 쿼리에서 matchQuery가 없을때 filter쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                filterQuery {
                    matchQuery { field = "a"; query = "" }
                    matchQuery { field = "b"; query = null }
                }
            }
        }


        val filterQuery = query.bool().filter()

        query.isBool shouldBe true
        filterQuery.size shouldBe 0
    }

    test("mustNot 쿼리에서 match 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                mustNotQuery {
                    matchQuery { field = "a"; query = "1111" }
                    matchQuery { field = "b"; query = "2222" }
                }
            }
        }

        val mustNotQuery = query.bool().mustNot()

        query.isBool shouldBe true
        mustNotQuery.size shouldBe 2
        mustNotQuery.filter { it.isMatch }.find { it.match().field() == "a" }?.match()?.query()?.stringValue() shouldBe "1111"
        mustNotQuery.filter { it.isMatch }.find { it.match().field() == "b" }?.match()?.query()?.stringValue() shouldBe "2222"
    }

    test("mustNot 쿼리에서 matchQuery에 query 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                mustNotQuery {
                    matchQuery { field = "a"; query = null }
                    matchQuery { field = "b"; query = "" }
                    matchQuery { field = "c"; query = "3333" }
                }
            }
        }

        val mustNotQuery = query.bool().mustNot()

        query.isBool shouldBe true
        mustNotQuery.size shouldBe 1
        mustNotQuery.filter { it.isMatch }.find { it.match().field() == "a" } shouldBe null
        mustNotQuery.filter { it.isMatch }.find { it.match().field() == "b" } shouldBe null
        mustNotQuery.filter { it.isMatch }.find { it.match().field() == "c" }?.match()?.query()?.stringValue() shouldBe "3333"
    }

    test("mustNot 쿼리에서 matchQuery가 없을때 mustNot쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                mustNotQuery {
                    matchQuery { field = "a"; query = "" }
                    matchQuery { field = "b"; query = null }
                }
            }
        }
        val mustNotQuery = query.bool().mustNot()

        query.isBool shouldBe true
        mustNotQuery.size shouldBe 0
    }

    test("should 쿼리에서 match 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                shouldQuery {
                    matchQuery { field = "a"; query = "1111" }
                    matchQuery { field = "b"; query = "2222" }
                }
            }
        }

        val shouldQuery = query.bool().should()

        query.isBool shouldBe true
        shouldQuery.size shouldBe 2
        shouldQuery.filter { it.isMatch }.find { it.match().field() == "a" }?.match()?.query()?.stringValue() shouldBe "1111"
        shouldQuery.filter { it.isMatch }.find { it.match().field() == "b" }?.match()?.query()?.stringValue() shouldBe "2222"
    }

    test("should 쿼리에서 matchQuery에 query 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                shouldQuery {
                    matchQuery { field = "a"; query = null }
                    matchQuery { field = "b"; query = "" }
                    matchQuery { field = "c"; query = "3333" }
                }
            }
        }

        val shouldQuery = query.bool().should()

        query.isBool shouldBe true
        shouldQuery.size shouldBe 1
        shouldQuery.filter { it.isMatch }.find { it.match().field() == "a" } shouldBe null
        shouldQuery.filter { it.isMatch }.find { it.match().field() == "b" } shouldBe null
        shouldQuery.filter { it.isMatch }.find { it.match().field() == "c" }?.match()?.query()?.stringValue() shouldBe "3333"
    }

    test("should 쿼리에서 matchQuery가 없을때 should쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                shouldQuery {
                    matchQuery { field = "a"; query = "" }
                    matchQuery { field = "b"; query = null }
                }
            }
        }

        val shouldQuery = query.bool().should()

        query.isBool shouldBe true
        shouldQuery.size shouldBe 0
    }

    test("match 쿼리에 boost 설정시 적용이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery { matchQuery { field = "a"; query = "1111"; boost = 2.0F } }
            }
        }

        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isMatch }.find { it.match().field() == "a" }?.match()?.query()?.stringValue() shouldBe "1111"
        mustQuery.filter { it.isMatch }.find { it.match().field() == "a" }?.match()?.boost() shouldBe 2.0F
    }

    test("match 쿼리에 _name이 설정되면 match.queryName에 반영되어야함") {
        val query = query {
            boolQuery {
                mustQuery { matchQuery { field = "a"; query = "1111"; _name = "named" } }
            }
        }

        val mustList = query.bool().must()

        query.isBool shouldBe true
        mustList.size shouldBe 1

        val match = mustList.first().match()
        match.field() shouldBe "a"
        match.query().stringValue() shouldBe "1111"
        match.queryName() shouldBe "named"
    }

    test("match 쿼리에서 operator, minimum_should_match, analyzer 설정이 적용되어야함") {
        val query = query {
            boolQuery {
                mustQuery { matchQuery { field = "title"; query = "quick brown fox"; operator = Operator.And; minimumShouldMatch = "2"; analyzer = "standard" } }
            }
        }

        val m = query.bool().must().first().match()
        m.operator() shouldBe Operator.And
        m.minimumShouldMatch() shouldBe "2"
        m.analyzer() shouldBe "standard"
    }

    test("match 쿼리에서 zero_terms_query, lenient, auto_generate_synonyms_phrase_query 설정이 적용되어야함") {
        val query = query {
            boolQuery {
                mustQuery { matchQuery { field = "content"; query = "the"; zeroTermsQuery = ZeroTermsQuery.All; lenient = true; autoGenerateSynonymsPhraseQuery = true } }
            }
        }

        val m = query.bool().must().first().match()
        m.zeroTermsQuery() shouldBe ZeroTermsQuery.All
        m.lenient() shouldBe true
        m.autoGenerateSynonymsPhraseQuery() shouldBe true
    }

    test("match 쿼리에서 fuzziness 관련 옵션 설정이 적용되어야함") {
        val query = query {
            boolQuery {
                mustQuery { matchQuery { field = "name"; query = "jon"; fuzziness = "AUTO"; prefixLength = 1; maxExpansions = 50; fuzzyTranspositions = true; fuzzyRewrite = "constant_score" } }
            }
        }

        val must = query.bool().must().first().match()
        must.fuzziness() shouldBe "AUTO"
        must.prefixLength() shouldBe 1
        must.maxExpansions() shouldBe 50
        must.fuzzyTranspositions() shouldBe true
        must.fuzzyRewrite() shouldBe "constant_score"
    }

    test("Bool 쿼리 없이 독립 Match 쿼리 생성이 되어야함") {
        val query = query {
            matchQuery {
                field = "title"
                query = "elasticsearch"
            }
        }

        query.isMatch shouldBe true
        query.match().field() shouldBe "title"
        query.match().query().stringValue() shouldBe "elasticsearch"
    }

    test("field가 null이거나 빈 문자열일 때 Match 쿼리가 생성되지 않아야함") {
        // field가 null인 경우
        val query1 = queryOrNull {
            matchQuery {
                field = null
                query = "test"
            }
        }
        query1 shouldBe null

        // field가 빈 문자열인 경우
        val query2 = queryOrNull {
            matchQuery {
                field = ""
                query = "test"
            }
        }
        query2 shouldBe null
    }

    test("복수 파라미터를 동시에 설정했을 때 모두 적용되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchQuery {
                        field = "content"
                        query = "elasticsearch search"
                        operator = Operator.And
                        fuzziness = "AUTO"
                        analyzer = "standard"
                        boost = 1.5F
                        _name = "complex_match"
                    }
                }
            }
        }

        val match = query.bool().must().first().match()
        match.field() shouldBe "content"
        match.query().stringValue() shouldBe "elasticsearch search"
        match.operator() shouldBe Operator.And
        match.fuzziness() shouldBe "AUTO"
        match.analyzer() shouldBe "standard"
        match.boost() shouldBe 1.5F
        match.queryName() shouldBe "complex_match"
    }
})
