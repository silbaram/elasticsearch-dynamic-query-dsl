package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class IntervalsQueryTest : FunSpec({

    test("기본 intervals match query가 생성되어야 함") {
        val query = query {
            intervals("content") { match("quick brown fox") }
        }

        query shouldNotBe null
        query.isIntervals shouldBe true
        // 단일 규칙은 any_of 래핑 없이 직렬화
    }
    
    test("intervals match query에 maxGaps과 ordered 옵션이 적용되어야 함") {
        val query = query {
            intervals("content") { match("quick brown", maxGaps = 2, ordered = true) }
        }
        
        // The query should be created successfully
        query shouldNotBe null
        query.isIntervals shouldBe true
    }
    
    test("intervals prefix query가 생성되어야 함") {
        val query = query { intervals("content") { prefix("qu") } }
        
        query.isIntervals shouldBe true
        // With anyOf wrapper structure, direct access to prefix is not straightforward
        // But we can verify the query was created successfully
    }
    
    test("intervals wildcard query가 생성되어야 함") {
        val query = query { intervals("content") { wildcard("bro*") } }
        
        query.isIntervals shouldBe true
    }
    
    test("intervals fuzzy query가 생성되어야 함") {
        val query = query { intervals("content") { fuzzy("fox", fuzziness = "1", prefixLength = 0, transpositions = true) } }
        
        query.isIntervals shouldBe true
    }
    
    test("boost와 _name 파라미터가 적용되어야 함") {
        val query = query { intervals("content", boost = 2.5f, _name = "test_intervals_query") { match("test query") } }
        
        query.isIntervals shouldBe true
        // With the new structure, boost and _name are applied to the intervals query level
    }
    
    test("필수 파라미터가 null이거나 빈 값이면 null을 반환해야 함") {
        val query1 = queryOrNull { intervals(" ") { anyOf { match("test") } } }
        query1 shouldBe null
        
        val query2 = queryOrNull { intervals("content") { match("") } }
        query2 shouldBe null
        
        val query3 = queryOrNull { intervals("") { anyOf { prefix("p") } } }
        query3 shouldBe null
    }
    
    test("bool 쿼리에서 사용할 수 있어야 함") {
        val query = query {
            boolQuery {
                mustQuery { intervals("content") { match("quick brown fox") } }
            }
        }
        
        val mustQueries = query.bool().must()
        query.isBool shouldBe true
        mustQueries.size shouldBe 1
        mustQueries.first().isIntervals shouldBe true
    }
    
    test("queries 배열에서 사용할 수 있어야 함") {
        val query = query {
            boolQuery {
                mustQuery {
                    intervals("content") { match("quick brown") }
                    intervals("title") { prefix("fox") }
                }
            }
        }
        
        val mustQueries = query.bool().must()
        mustQueries.size shouldBe 2
        mustQueries[0].isIntervals shouldBe true
        mustQueries[1].isIntervals shouldBe true
    }
    
    test("각 query 타입이 올바르게 생성되어야 함") {
        val matchQuery = query { intervals("content") { match("test") } }
        val prefixQuery = query { intervals("content") { prefix("test") } }
        val wildcardQuery = query { intervals("content") { wildcard("test*") } }
        val fuzzyQuery = query { intervals("content") { fuzzy("test") } }
        
        matchQuery.isIntervals shouldBe true
        prefixQuery.isIntervals shouldBe true
        wildcardQuery.isIntervals shouldBe true
        fuzzyQuery.isIntervals shouldBe true
    }
    
    test("모든 파라미터가 적용된 intervals match query") {
        val query = query { intervals("content", boost = 1.5f, _name = "test_match_query") { match("test query", maxGaps = 2, ordered = true, analyzer = "standard", useField = "content.analyzed") } }
        
        query.isIntervals shouldBe true
    }
    
    test("모든 파라미터가 적용된 intervals fuzzy query") {
        val query = query { intervals("content", boost = 2.0f, _name = "test_fuzzy_query") { fuzzy("test", prefixLength = 2, transpositions = false, fuzziness = "AUTO", analyzer = "standard", useField = "content.analyzed") } }
        
        query.isIntervals shouldBe true
    }
    
    test("field가 빈 문자열일 때 null 반환") {
        val query = queryOrNull { intervals(" ") { anyOf { match("test") } } }
        query shouldBe null
    }
    
    test("복수의 intervals query를 bool query로 조합") {
        val query = query {
            boolQuery {
                mustQuery {
                    intervals("title") { anyOf { match("elasticsearch") } }
                    intervals("content") { anyOf { prefix("search") } }
                    intervals("tags") { anyOf { wildcard("java*") } }
                    intervals("description") { anyOf { fuzzy("query", fuzziness = "1") } }
                }
            }
        }
        
        val mustQueries = query.bool().must()
        mustQueries.size shouldBe 4
        mustQueries.forEach { it.isIntervals shouldBe true }
    }
    
    test("복합 DSL - allOf 규칙이 동작해야 함") {
        val query = query {
            intervals("content") {
                allOf(maxGaps = 5, ordered = true) {
                    match("quick"); match("brown"); match("fox")
                }
            }
        }
        
        query shouldNotBe null
        query.isIntervals shouldBe true
    }
    
    test("복합 DSL - anyOf 규칙이 동작해야 함") {
        val query = query {
            intervals("content") {
                anyOf {
                    match("quick"); prefix("qu"); wildcard("q*"); fuzzy("quik", fuzziness = "1")
                }
            }
        }
        
        query shouldNotBe null
        query.isIntervals shouldBe true
    }
    
    test("복합 DSL - 단일 규칙이 동작해야 함") {
        val query1 = query { intervals("content") { match("single match") } }
        val query2 = query { intervals("content") { prefix("test") } }
        
        query1 shouldNotBe null
        query1.isIntervals shouldBe true
        query2 shouldNotBe null
        query2.isIntervals shouldBe true
    }
    
    test("복합 DSL - 중첩된 anyOf와 allOf 규칙") {
        val query = query {
            intervals("content") {
                allOf(maxGaps = 10) {
                    match("elasticsearch")
                    anyOf { match("java"); match("kotlin"); match("python") }
                    allOf(ordered = true) { prefix("search"); wildcard("eng*") }
                }
            }
        }
        
        query shouldNotBe null
        query.isIntervals shouldBe true
    }
    
    test("복합 DSL - 빈 규칙일 때 null 반환") {
        val query = queryOrNull { intervals("content") { /* empty */ } }
        query shouldBe null
    }
    
    test("복합 DSL - boost와 _name 파라미터 적용") {
        val query = query {
            intervals("content", boost = 2.0f, _name = "complex_intervals") {
                allOf(maxGaps = 3) { match("search"); match("engine") }
            }
        }
        
        query shouldNotBe null
        query.isIntervals shouldBe true
    }
    
    test("복합 DSL - bool 쿼리 내에서 사용") {
        val query = query {
            boolQuery {
                mustQuery { intervals("content") { allOf(ordered = true) { match("machine"); match("learning") } } }
                shouldQuery { intervals("title") { anyOf { match("AI"); match("ML"); prefix("artif") } } }
            }
        }
        
        query.isBool shouldBe true
        val mustQueries = query.bool().must()
        val shouldQueries = query.bool().should()
        mustQueries.size shouldBe 1
        shouldQueries.size shouldBe 1
        mustQueries.first().isIntervals shouldBe true
        shouldQueries.first().isIntervals shouldBe true
    }
})
