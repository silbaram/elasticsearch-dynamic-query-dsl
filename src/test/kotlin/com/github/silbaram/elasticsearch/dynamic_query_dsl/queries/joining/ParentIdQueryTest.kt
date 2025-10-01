package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.joining

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class ParentIdQueryTest : FunSpec({

    test("parent_id 쿼리가 id와 type을 설정해야 함") {
        val result = query {
            boolQuery {
                mustQuery {
                    parentIdQuery {
                        id = "parent-1"
                        type = "reply"
                        ignoreUnmapped = true
                        boost = 0.8f
                        _name = "parent-filter"
                    }
                }
            }
        }

        val must = result.bool().must()
        must.size shouldBe 1
        val parentId = must.first()
        parentId.isParentId shouldBe true
        val pid = parentId.parentId()
        pid.id() shouldBe "parent-1"
        pid.type() shouldBe "reply"
        pid.ignoreUnmapped() shouldBe true
        pid.boost() shouldBe 0.8f
        pid.queryName() shouldBe "parent-filter"
    }

    test("id가 없으면 parent_id 쿼리가 생성되지 않아야 함") {
        queryOrNull {
            parentIdQuery {
                id = ""
                type = "reply"
            }
        }.shouldBeNull()
    }
})
