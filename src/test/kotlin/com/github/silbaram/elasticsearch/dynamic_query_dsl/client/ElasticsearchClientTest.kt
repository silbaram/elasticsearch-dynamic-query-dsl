package com.github.silbaram.elasticsearch.dynamic_query_dsl.client

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.*
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking

/**
 * Elasticsearch 클라이언트 테스트
 * 
 * 주의: 이 테스트는 실제 Elasticsearch 클러스터가 필요합니다.
 * 로컬에서 Elasticsearch를 실행하거나 테스트를 비활성화하세요.
 */
class ElasticsearchClientTest : FunSpec({

    // 테스트용 데이터 클래스
    data class Product(
        val id: String,
        val name: String,
        val category: String,
        val price: Double,
        val description: String,
        val inStock: Boolean
    )

    val testProducts = listOf(
        Product("1", "MacBook Pro", "laptop", 2999.99, "Apple MacBook Pro with M2 chip", true),
        Product("2", "iPad Air", "tablet", 599.99, "Apple iPad Air with stunning display", true),
        Product("3", "iPhone 15", "smartphone", 999.99, "Latest iPhone with advanced camera", false),
        Product("4", "Samsung Galaxy S24", "smartphone", 899.99, "Samsung flagship smartphone", true)
    )

    // 실제 Elasticsearch 연결이 필요한 테스트들
    // 로컬에 Elasticsearch가 없으면 이 테스트들을 주석 처리하세요
    xcontext("실제 Elasticsearch 연결 테스트 (로컬 ES 필요)") {

        val client = ElasticsearchClientWrapper.create()
        val testIndex = "test-products"

        beforeEach {
            // 테스트 인덱스가 있으면 삭제
            if (client.indexExists(testIndex)) {
                client.deleteIndex(testIndex)
            }
            
            // 테스트 인덱스 생성
            client.createIndex(
                index = testIndex,
                mappings = mapOf(
                    "id" to mapOf("type" to "keyword"),
                    "name" to mapOf("type" to "text"),
                    "category" to mapOf("type" to "keyword"),
                    "price" to mapOf("type" to "double"),
                    "description" to mapOf("type" to "text"),
                    "inStock" to mapOf("type" to "boolean")
                )
            )
            
            // 테스트 데이터 인덱싱
            client.bulkIndex(testIndex, testProducts) { it.id }
            
            // 인덱싱 완료까지 잠시 대기
            Thread.sleep(1000)
        }

        afterEach {
            // 테스트 인덱스 정리
            if (client.indexExists(testIndex)) {
                client.deleteIndex(testIndex)
            }
        }

        test("클러스터 헬스 체크") {
            val health = client.health()
            health.isHealthy shouldBe true
            health.clusterName shouldNotBe null
        }

        test("DSL을 사용한 기본 검색") {
            val response = client.search<Product> {
                indices(testIndex)
                query {
                    matchQuery {
                        field = "name"
                        query = "MacBook"
                    }
                }
                size(10)
            }

            response.totalHits shouldBe 1
            response.hits.first().source?.name shouldBe "MacBook Pro"
        }

        test("복합 쿼리 검색") {
            val response = client.search<Product> {
                indices(testIndex)
                query {
                    boolQuery {
                        mustQuery {
                            termQuery {
                                field = "category"
                                value = "smartphone"
                            }
                        }
                        mustQuery {
                            termQuery {
                                field = "inStock"
                                value = "true"
                            }
                        }
                    }
                }
            }

            response.totalHits shouldBe 1
            response.hits.first().source?.name shouldBe "Samsung Galaxy S24"
        }

        test("정렬과 페이징") {
            val response = client.search<Product> {
                indices(testIndex)
                query {
                    matchQuery {
                        field = "category"
                        query = "smartphone tablet laptop"
                        operator = co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or
                    }
                }
                sortByField("price", co.elastic.clients.elasticsearch._types.SortOrder.Desc)
                size(2)
                from(0)
            }

            response.totalHits shouldBe 4
            response.hits.size shouldBe 2
            response.hits.first().source?.price shouldBe 2999.99 // MacBook Pro (가장 비싼 제품)
        }

        test("필드 필터링") {
            val response = client.search<Product> {
                indices(testIndex)
                query {
                    termQuery {
                        field = "category"
                        value = "laptop"
                    }
                }
                includeFields("name", "price")
                excludeFields("description")
            }

            response.totalHits shouldBe 1
            response.hits.first().source?.name shouldBe "MacBook Pro"
        }

        test("비동기 검색") {
            runBlocking {
                val response = client.searchAsync<Product> {
                    indices(testIndex)
                    query {
                        matchQuery {
                            field = "name"
                            query = "iPad"
                        }
                    }
                }

                response.totalHits shouldBe 1
                response.hits.first().source?.name shouldBe "iPad Air"
            }
        }

        test("페이징 정보 확인") {
            val response = client.search<Product> {
                indices(testIndex)
                query {
                    matchQuery {
                        field = "name"
                        query = "*"
                    }
                }
                size(2)
                from(0)
            }

            val pagingInfo = response.getPagingInfo(1, 2)
            pagingInfo.totalHits shouldBe 4
            pagingInfo.totalPages shouldBe 2
            pagingInfo.hasNext shouldBe true
            pagingInfo.hasPrevious shouldBe false
        }
    }

    // 모킹이나 단위 테스트를 위한 컨텍스트
    context("설정 및 빌더 테스트") {

        test("클라이언트 설정 - 로컬") {
            val config = ElasticsearchClientConfig.local()
            config.hosts shouldBe listOf("localhost:9200")
            config.protocol shouldBe "http"
            config.enableSsl shouldBe false
        }

        test("클라이언트 설정 - 운영") {
            val config = ElasticsearchClientConfig.production(
                hosts = listOf("es-node1:9200", "es-node2:9200"),
                username = "elastic",
                password = "password"
            )
            config.hosts shouldBe listOf("es-node1:9200", "es-node2:9200")
            config.protocol shouldBe "https"
            config.enableSsl shouldBe true
            config.username shouldBe "elastic"
            config.password shouldBe "password"
        }

        test("검색 요청 빌더") {
            val request = searchRequest {
                indices("test-index")
                query {
                    matchQuery {
                        field = "title"
                        query = "elasticsearch"
                    }
                }
                size(20)
                from(10)
                sortByField("created_at")
                includeFields("title", "content")
            }

            request.index() shouldBe listOf("test-index")
            request.size() shouldBe 20
            request.from() shouldBe 10
        }

        test("인덱스 생성 빌더 - 매핑과 설정 반영") {
            val request = buildCreateIndexRequest(
                index = "products",
                mappings = mapOf(
                    "id" to mapOf("type" to "keyword"),
                    "price" to mapOf("type" to "double")
                ),
                settings = mapOf("number_of_shards" to 1)
            )

            request.index() shouldBe "products"
            request.mappings()?.properties()?.containsKey("id") shouldBe true
            request.mappings()?.properties()?.containsKey("price") shouldBe true
            request.settings() shouldNotBe null
        }
    }
})
