package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MoreLikeThisQueryTest : FunSpec({

    test("최상위 more_like_this 쿼리 생성 - text 기준") {
        val q = query {
            mlt {
                like { text("kotlin coroutine", "typed dsl") }
                fields("title", "body")
                analyzer = "standard"
                minTermFreq = 1
                maxQueryTerms = 25
                minimumShouldMatch = "30%"
                stopWords("a", "the")
                boost = 1.1f
                _name = "mlt_text"
            }
        }

        q.isMoreLikeThis shouldBe true
        val mlt = q.moreLikeThis()
        mlt.fields().size shouldBe 2
        mlt.analyzer() shouldBe "standard"
        mlt.minTermFreq() shouldBe 1
        mlt.maxQueryTerms() shouldBe 25
        mlt.minimumShouldMatch() shouldBe "30%"
        mlt.stopWords().size shouldBe 2
        mlt.boost() shouldBe 1.1f
        mlt.queryName() shouldBe "mlt_text"
        mlt.like().size shouldBe 2
        // 첫 like는 텍스트여야 함
        mlt.like()[0].text() shouldBe "kotlin coroutine"
    }

    test("문서 기반 like/unlike 혼합 및 must 절에서의 생성/생략") {
        val q = query {
            boolQuery {
                mustQuery {
                    // 생성됨
                    moreLikeThis {
                        like { doc("articles", "1") }
                        unlike { text("legacy") }
                        fields("title")
                    }
                    // 생략됨: like이 비어있음
                    moreLikeThis { /* no like */ }
                }
            }
        }

        q.isBool shouldBe true
        val must = q.bool().must()
        must.size shouldBe 1
        val mlt = must.first().moreLikeThis()
        mlt.like().size shouldBe 1
        mlt.unlike().size shouldBe 1
        // 문서 like 검증 (index/id)
        mlt.like().first().document().index() shouldBe "articles"
        mlt.like().first().document().id() shouldBe "1"
    }

    test("invalid 입력은 생략되어야 함 (blank 텍스트, 잘못된 문서 참조)") {
        val q = queryOrNull {
            mlt {
                like { text(" "); doc("", ""); doc("idx", "") }
            }
        }
        q shouldBe null
    }
})
