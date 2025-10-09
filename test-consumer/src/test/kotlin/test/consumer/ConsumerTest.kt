package test.consumer

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * 소비자 프로젝트 테스트
 *
 * 목적: elasticsearch-dynamic-query-dsl 라이브러리만 의존성으로 추가했을 때
 *      elasticsearch-java와 kotlinx-coroutines-core가 전이 의존성으로 자동 포함되는지 검증
 *
 * 실패 시나리오: 전이 의존성이 없으면 아래 에러 발생
 * - "Cannot access class 'Query.Builder'"
 * - "Unresolved reference: delay"
 */
class ConsumerTest : FunSpec({

    test("Query.Builder 클래스에 접근 가능해야 함") {
        // Query.Builder는 elasticsearch-java 라이브러리의 클래스
        // 전이 의존성이 제대로 설정되지 않으면 컴파일 에러 발생
        val builder = Query.Builder()
        builder shouldNotBe null
    }

    test("Bool 쿼리를 생성할 수 있어야 함") {
        val query = query {
            boolQuery {
                mustQuery {
                    matchQuery {
                        field = "title"
                        query = "elasticsearch"
                    }
                }
            }
        }

        query shouldNotBe null
        query.isBool shouldBe true
        query.bool().must().size shouldBe 1
    }

    test("Match 쿼리를 생성할 수 있어야 함") {
        val query = query {
            matchQuery {
                field = "content"
                query = "kotlin"
                operator = co.elastic.clients.elasticsearch._types.query_dsl.Operator.And
            }
        }

        query shouldNotBe null
        query.isMatch shouldBe true
        query.match().field() shouldBe "content"
        query.match().query().stringValue() shouldBe "kotlin"
    }

    test("코루틴 API를 사용할 수 있어야 함") {
        // kotlinx-coroutines-core가 전이 의존성으로 포함되어야 delay 함수 사용 가능
        runBlocking {
            val startTime = System.currentTimeMillis()
            delay(100)
            val endTime = System.currentTimeMillis()

            // 최소 100ms 이상 지연되었는지 확인
            (endTime - startTime >= 100) shouldBe true
        }
    }
})
