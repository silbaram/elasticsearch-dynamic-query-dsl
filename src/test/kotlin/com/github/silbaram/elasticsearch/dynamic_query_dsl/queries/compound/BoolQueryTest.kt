package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BoolQueryTest: FunSpec({

    test("bool에 filter query가 추가 되어야함") {
        val q = query {
            boolQuery {
                filterQuery {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
            }
        }

        q.isBool shouldBe true
        q.bool().filter().size shouldBe 1
        q.bool().must().size shouldBe 0
        q.bool().mustNot().size shouldBe 0
        q.bool().should().size shouldBe 0
    }

    test("bool에 must query가 추가 되어야함") {
        val q = query {
            boolQuery {
                mustQuery {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
            }
        }

        q.isBool shouldBe true
        q.bool().filter().size shouldBe 0
        q.bool().must().size shouldBe 1
        q.bool().mustNot().size shouldBe 0
        q.bool().should().size shouldBe 0
    }

    test("bool에 mustNot query가 추가 되어야함") {
        val q = query {
            boolQuery {
                mustNotQuery {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
            }
        }

        q.isBool shouldBe true
        q.bool().filter().size shouldBe 0
        q.bool().must().size shouldBe 0
        q.bool().mustNot().size shouldBe 1
        q.bool().should().size shouldBe 0
    }

    test("bool에 should query가 추가 되어야함") {
        val q = query {
            boolQuery {
                shouldQuery {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
            }
        }

        q.isBool shouldBe true
        q.bool().filter().size shouldBe 0
        q.bool().must().size shouldBe 0
        q.bool().mustNot().size shouldBe 0
        q.bool().should().size shouldBe 1
    }

    test("bool에 filter, must, mustNot, should query가 추가 되어야함") {
        val q = query {
            boolQuery {
                filterQuery {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
                mustQuery {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
                mustNotQuery {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
                shouldQuery {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
            }
        }

        q.isBool shouldBe true
        q.bool().filter().size shouldBe 1
        q.bool().must().size shouldBe 1
        q.bool().mustNot().size shouldBe 1
        q.bool().should().size shouldBe 1
    }

    test("bool에 minimumShouldMatch, boost가 추가 되어야함") {
        val q = query {
            boolQuery {
                filterQuery {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
                mustQuery {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
                mustNotQuery {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
                shouldQuery {
                    termQuery(
                        field = "field1",
                        value = "value1"
                    )
                }
                minimumShouldMatch("2")
                boost(2.0F)
            }
        }

        q.isBool shouldBe true
        q.bool().filter().size shouldBe 1
        q.bool().must().size shouldBe 1
        q.bool().mustNot().size shouldBe 1
        q.bool().should().size shouldBe 1
        q.bool().minimumShouldMatch() shouldBe "2"
        q.bool().boost() shouldBe 2.0F
    }
})
