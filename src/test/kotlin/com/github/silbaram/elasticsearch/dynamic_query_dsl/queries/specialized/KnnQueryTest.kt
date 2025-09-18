package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class KnnQueryTest : FunSpec({

    test("최상위 knn 쿼리 생성 및 속성 확인") {
        val q = query {
            knnQuery {
                field = "embedding"
                queryVector = listOf(0.1f, -0.2f, 0.3f)
                k = 5
                numCandidates = 50
            }
        }

        q.isKnn shouldBe true
        val kq = q.knn()
        kq.field() shouldBe "embedding"
        kq.queryVector().size shouldBe 3
        kq.numCandidates() shouldBe 50
    }

    test("8.14 클라이언트 호환: k 없이도 생성 가능") {
        val q = query {
            knnQuery {
                field = "vec"
                queryVector = listOf(0.2f, 0.3f)
                numCandidates = 20
            }
        }

        q.isKnn shouldBe true
        q.knn().numCandidates() shouldBe 20
    }

    test("knn 쿼리 filter 조합 - 단일 필터") {
        val q = query {
            knnQuery {
                field = "vec"
                queryVector = listOf(0.01f, 0.02f)
                k = 3
                numCandidates = 20
                filter { termQuery { field = "status"; value = "active" } }
            }
        }

        q.isKnn shouldBe true
        val filters = q.knn().filter()
        filters.size shouldBe 1
        filters.first().isTerm shouldBe true
    }

    test("knn 쿼리 filter 조합 - 다중 필터는 bool.filter로 래핑") {
        val q = query {
            knnQuery {
                field = "vec"
                queryVector = listOf(0.0f, 0.1f)
                k = 10
                numCandidates = 100
                filter {
                    termQuery { field = "category"; value = "electronics" }
                    termQuery { field = "brand"; value = "acme" }
                }
            }
        }

        val filters = q.knn().filter()
        filters.size shouldBe 1
        val boolFilter = filters.first()
        boolFilter.isBool shouldBe true
        boolFilter.bool().filter().size shouldBe 2
    }

    test("유효성 검사: 필수 값이 없으면 생략") {
        // field가 비었으면 null
        queryOrNull { knnQuery { field = ""; queryVector = listOf(0.1f); k = 1; numCandidates = 10 } } shouldBe null
        // queryVector가 비면 null
        queryOrNull { knnQuery { field = "v"; queryVector = emptyList(); k = 1; numCandidates = 10 } } shouldBe null
        // queryVector에 null 요소가 있으면 null
        queryOrNull { knnQuery { field = "v"; queryVector = listOf(0.1f, null); k = 1; numCandidates = 10 } } shouldBe null
        // k가 주어졌지만 유효하지 않으면 생략
        queryOrNull { knnQuery { field = "v"; queryVector = listOf(0.1f); k = 0; numCandidates = 10 } } shouldBe null
        // numCandidates는 필수
        queryOrNull { knnQuery { field = "v"; queryVector = listOf(0.1f); k = 1; numCandidates = 0 } } shouldBe null
    }
})
