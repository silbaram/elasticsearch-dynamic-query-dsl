package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.filterQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustNotQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TermQueryTest : FunSpec({

    test("must 쿼리에서 term 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        termQuery(
                            field = "a",
                            value = "1111"
                        ),
                        termQuery(
                            field = "b",
                            value = "2222"
                        )
                    ]
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 2
        mustQuery.filter { it.isTerm }.find { it.term().field() == "a" }?.term()?.value()?.stringValue() shouldBe "1111"
        mustQuery.filter { it.isTerm }.find { it.term().field() == "b" }?.term()?.value()?.stringValue() shouldBe "2222"
    }

    test("must 쿼리에서 termQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        termQuery(
                            field = "a",
                            value = null
                        ),
                        termQuery(
                            field = "b",
                            value = ""
                        ),
                        termQuery(
                            field = "c",
                            value = "3333"
                        )
                    ]
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isTerm }.find { it.term().field() == "a" } shouldBe null
        mustQuery.filter { it.isTerm }.find { it.term().field() == "b" } shouldBe null
        mustQuery.filter { it.isTerm }.find { it.term().field() == "c" }?.term()?.value()?.stringValue() shouldBe "3333"
    }

    test("must 쿼리에서 termQuery 송성값이 없어 생성이 안되어 하위 쿼리가 없을때 must쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    queries[
                        termQuery(
                            field = "a",
                            value = ""
                        ),
                        termQuery(
                            field = "b",
                            value = null
                        )
                    ]
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 0
    }

    test("filter 쿼리에서 term 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                filterQuery {
                    queries[
                        termQuery(
                            field = "a",
                            value = "1111"
                        ),
                        termQuery(
                            field = "b",
                            value = "2222"
                        )
                    ]
                }
            }
        }
        val filterQuery = query.bool().filter()

        query.isBool shouldBe true
        filterQuery.size shouldBe 2
        filterQuery.filter { it.isTerm }.find { it.term().field() == "a" }?.term()?.value()?.stringValue() shouldBe "1111"
        filterQuery.filter { it.isTerm }.find { it.term().field() == "b" }?.term()?.value()?.stringValue() shouldBe "2222"
    }

    test("filter 쿼리에서 termQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                filterQuery {
                    queries[
                        termQuery(
                            field = "a",
                            value = null
                        ),
                        termQuery(
                            field = "b",
                            value = ""
                        ),
                        termQuery(
                            field = "c",
                            value = "3333"
                        )
                    ]
                }
            }
        }
        val filterQuery = query.bool().filter()

        query.isBool shouldBe true
        filterQuery.size shouldBe 1
        filterQuery.filter { it.isTerm }.find { it.term().field() == "a" } shouldBe null
        filterQuery.filter { it.isTerm }.find { it.term().field() == "b" } shouldBe null
        filterQuery.filter { it.isTerm }.find { it.term().field() == "c" }?.term()?.value()?.stringValue() shouldBe "3333"
    }

    test("filter 쿼리에서 termQuery가 없을때 must쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                filterQuery {
                    queries[
                        termQuery(
                            field = "a",
                            value = ""
                        ),
                        termQuery(
                            field = "b",
                            value = null
                        )
                    ]
                }
            }
        }
        val filterQuery = query.bool().filter()

        query.isBool shouldBe true
        filterQuery.size shouldBe 0
    }

    test("mustNot 쿼리에서 term 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                mustNotQuery {
                    queries[
                        termQuery(
                            field = "a",
                            value = "1111"
                        ),
                        termQuery(
                            field = "b",
                            value = "2222"
                        )
                    ]
                }
            }
        }
        val mustNotQuery = query.bool().mustNot()

        query.isBool shouldBe true
        mustNotQuery.size shouldBe 2
        mustNotQuery.filter { it.isTerm }.find { it.term().field() == "a" }?.term()?.value()?.stringValue() shouldBe "1111"
        mustNotQuery.filter { it.isTerm }.find { it.term().field() == "b" }?.term()?.value()?.stringValue() shouldBe "2222"
    }

    test("mustNot 쿼리에서 termQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                mustNotQuery {
                    queries[
                        termQuery(
                            field = "a",
                            value = null
                        ),
                        termQuery(
                            field = "b",
                            value = ""
                        ),
                        termQuery(
                            field = "c",
                            value = "3333"
                        )
                    ]
                }
            }
        }
        val mustNotQuery = query.bool().mustNot()

        query.isBool shouldBe true
        mustNotQuery.size shouldBe 1
        mustNotQuery.filter { it.isTerm }.find { it.term().field() == "a" } shouldBe null
        mustNotQuery.filter { it.isTerm }.find { it.term().field() == "b" } shouldBe null
        mustNotQuery.filter { it.isTerm }.find { it.term().field() == "c" }?.term()?.value()?.stringValue() shouldBe "3333"
    }

    test("mustNot 쿼리에서 termQuery가 없을때 must쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                mustNotQuery {
                    queries[
                        termQuery(
                            field = "a",
                            value = ""
                        ),
                        termQuery(
                            field = "b",
                            value = null
                        )
                    ]
                }
            }
        }
        val mustNotQuery = query.bool().mustNot()
        
        query.isBool shouldBe true
        mustNotQuery.size shouldBe 0
    }

    test("should 쿼리에서 term 쿼리 생성이 되어야함") {
        val query = query {
            boolQuery {
                shouldQuery {
                    queries[
                        termQuery(
                            field = "a",
                            value = "1111"
                        ),
                        termQuery(
                            field = "b",
                            value = "2222"
                        )
                    ]
                }
            }
        }
        val shouldQuery = query.bool().should()

        query.isBool shouldBe true
        shouldQuery.size shouldBe 2
        shouldQuery.filter { it.isTerm }.find { it.term().field() == "a" }?.term()?.value()?.stringValue() shouldBe "1111"
        shouldQuery.filter { it.isTerm }.find { it.term().field() == "b" }?.term()?.value()?.stringValue() shouldBe "2222"
    }

    test("should 쿼리에서 termQuery에 value 값이 비었거나 null면 제외가 되어야함") {
        val query = query {
            boolQuery {
                shouldQuery {
                    queries[
                        termQuery(
                            field = "a",
                            value = null
                        ),
                        termQuery(
                            field = "b",
                            value = ""
                        ),
                        termQuery(
                            field = "c",
                            value = "3333"
                        )
                    ]
                }
            }
        }
        val shouldQuery = query.bool().should()

        query.isBool shouldBe true
        shouldQuery.size shouldBe 1
        shouldQuery.filter { it.isTerm }.find { it.term().field() == "a" } shouldBe null
        shouldQuery.filter { it.isTerm }.find { it.term().field() == "b" } shouldBe null
        shouldQuery.filter { it.isTerm }.find { it.term().field() == "c" }?.term()?.value()?.stringValue() shouldBe "3333"
    }

    test("should 쿼리에서 termQuery가 없을때 must쿼리는 생성 안되야함") {
        val query = query {
            boolQuery {
                shouldQuery {
                    queries[
                        termQuery(
                            field = "a",
                            value = ""
                        ),
                        termQuery(
                            field = "b",
                            value = null
                        )
                    ]
                }
            }
        }
        val shouldQuery = query.bool().should()

        query.isBool shouldBe true
        shouldQuery.size shouldBe 0
    }

    test("term 쿼리에 boost 설정시 적용이 되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    termQuery(
                        field = "a",
                        value = "1111",
                        boost = 2.0F
                    )
                }
            }
        }
        val mustQuery = query.bool().must()

        query.isBool shouldBe true
        mustQuery.size shouldBe 1
        mustQuery.filter { it.isTerm }.find { it.term().field() == "a" }?.term()?.value()?.stringValue() shouldBe "1111"
        mustQuery.filter { it.isTerm }.find { it.term().field() == "a" }?.term()?.boost() shouldBe 2.0F
    }

    test("term 쿼리에 _name이 설정되면 term.queryName에 반영되어야함") {
        val query = query {
            boolQuery {
                mustQuery {
                    termQuery(
                        field = "a",
                        value = "1111",
                        _name = "named"
                    )
                }
            }
        }
        val mustList = query.bool().must()

        query.isBool shouldBe true
        mustList.size shouldBe 1

        val term = mustList.first().term()
        term.field() shouldBe "a"
        term.value().stringValue() shouldBe "1111"
        term.queryName() shouldBe "named"
    }
})
