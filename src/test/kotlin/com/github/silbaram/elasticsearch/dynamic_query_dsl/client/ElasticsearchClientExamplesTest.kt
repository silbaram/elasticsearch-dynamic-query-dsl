package com.github.silbaram.elasticsearch.dynamic_query_dsl.client

import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.mustQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.clauses.shouldQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.boolQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.matchQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.rangeQuery
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.termQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.*
import io.kotest.matchers.collections.shouldContain
import kotlinx.coroutines.runBlocking

/**
 * Elasticsearch 클라이언트 사용 예제 테스트
 * 
 * 이 테스트들은 실제 Elasticsearch 클러스터 연결 없이도 
 * 클라이언트 사용법을 보여주는 예제 코드입니다.
 */
class ElasticsearchClientExamplesTest : FunSpec({

    // 샘플 데이터 클래스
    data class Product(
        val id: String,
        val name: String,
        val category: String,
        val price: Double,
        val description: String,
        val inStock: Boolean,
        val rating: Double,
        val tags: List<String>
    )

    data class User(
        val id: String,
        val username: String,
        val email: String,
        val age: Int,
        val active: Boolean
    )

    context("기본 사용법 예제") {
        
        test("클라이언트 설정 - 로컬 환경") {
            val config = ElasticsearchClientConfig.local()
            config.hosts shouldBe listOf("localhost:9200")
            config.protocol shouldBe "http"
            config.enableSsl shouldBe false
        }

        test("클라이언트 설정 - 운영 환경") {
            val config = ElasticsearchClientConfig.production(
                hosts = listOf("es-node1:9200", "es-node2:9200"),
                username = "elastic",
                password = "changeme"
            )
            config.hosts shouldBe listOf("es-node1:9200", "es-node2:9200")
            config.protocol shouldBe "https"
            config.enableSsl shouldBe true
            config.username shouldBe "elastic"
            config.password shouldBe "changeme"
        }

        test("클라이언트 설정 - 환경변수") {
            // 환경변수가 설정되어 있지 않을 수 있으므로 기본값 확인
            val config = ElasticsearchClientConfig.fromEnvironment()
            config.hosts shouldNotBe null
            listOf("http", "https") shouldContain config.protocol
        }

        test("기본 검색 요청 구조") {
            val request = searchRequest {
                indices("products")
                query {
                    matchQuery {
                        field = "name"
                        query = "laptop"
                    }
                }
                size(10)
                from(0)
            }

            request.index() shouldBe listOf("products")
            request.size() shouldBe 10
            request.from() shouldBe 0
        }

        test("복합 쿼리 요청 구조") {
            val request = searchRequest {
                indices("products")
                query {
                    boolQuery {
                        mustQuery {
                            termQuery {
                                field = "category"
                                value = "electronics"
                            }
                        }
                        mustQuery {
                            rangeQuery {
                                field = "price"
                                gte = 100.0
                                lte = 1000.0
                            }
                        }
                        shouldQuery {
                            matchQuery {
                                field = "description"
                                query = "high quality"
                            }
                        }
                    }
                }
                sortByField("price", co.elastic.clients.elasticsearch._types.SortOrder.Asc)
                size(20)
                from(0)
            }

            request.index() shouldBe listOf("products")
            request.size() shouldBe 20
            request.from() shouldBe 0
        }

        test("정렬과 필드 필터링 요청") {
            val request = searchRequest {
                indices("products")
                query {
                    matchQuery {
                        field = "name"
                        query = "smartphone"
                    }
                }
                includeFields("id", "name", "price", "rating")
                excludeFields("description")
                sortByField("rating", co.elastic.clients.elasticsearch._types.SortOrder.Desc)
                sortByScore()
                size(5)
            }

            request.index() shouldBe listOf("products")
            request.size() shouldBe 5
        }
    }

    context("클라이언트 생성 패턴") {
        
        test("기본 클라이언트 생성") {
            // 실제 연결하지 않고 설정만 확인
            val config = ElasticsearchClientConfig.local()
            config.hosts shouldBe listOf("localhost:9200")
        }

        test("환경변수 기반 클라이언트 설정") {
            val config = ElasticsearchClientConfig.fromEnvironment()
            // 기본값들이 제대로 설정되는지 확인
            config.connectTimeoutMillis shouldBe 5000
            config.socketTimeoutMillis shouldBe 60000
        }

        test("커스텀 클라이언트 설정") {
            val config = ElasticsearchClientConfig(
                hosts = listOf("localhost:9200", "localhost:9201"),
                protocol = "http",
                username = "test_user",
                password = "test_pass",
                connectTimeoutMillis = 10000,
                socketTimeoutMillis = 120000,
                enableSsl = false
            )

            config.hosts shouldBe listOf("localhost:9200", "localhost:9201")
            config.protocol shouldBe "http"
            config.username shouldBe "test_user"
            config.password shouldBe "test_pass"
            config.connectTimeoutMillis shouldBe 10000
            config.socketTimeoutMillis shouldBe 120000
            config.enableSsl shouldBe false
        }
    }

    context("검색 요청 DSL 예제") {
        
        test("페이징 처리") {
            val page = 3
            val pageSize = 10
            val request = searchRequest {
                indices("products")
                query {
                    matchQuery {
                        field = "category"
                        query = "laptop"
                    }
                }
                size(pageSize)
                from((page - 1) * pageSize)
                sortByScore()
            }

            request.size() shouldBe 10
            request.from() shouldBe 20 // 3페이지는 offset 20
        }

        test("멀티 인덱스 검색") {
            val request = searchRequest {
                indices("products-2023", "products-2024", "products-2025")
                query {
                    boolQuery {
                        mustQuery {
                            rangeQuery {
                                field = "price"
                                gte = 500.0
                            }
                        }
                    }
                }
            }

            request.index() shouldBe listOf("products-2023", "products-2024", "products-2025")
        }

        test("타임아웃 설정") {
            val request = searchRequest {
                indices("large-dataset")
                query {
                    matchQuery {
                        field = "content"
                        query = "elasticsearch"
                    }
                }
                timeout("30s")
                trackTotalHits(false) // 성능 최적화
            }

            request.timeout() shouldBe "30s"
        }
    }

    // 실제 Elasticsearch가 필요한 테스트들은 비활성화
    xcontext("실제 Elasticsearch 연결 예제 (ES 클러스터 필요)") {
        
        test("기본 검색 실행 예제") {
            // 실제 사용 시:
            // val client = ElasticsearchClientWrapper.create()
            // val response = client.search<Product> { ... }
            // client.close()
        }

        test("비동기 검색 예제") {
            // 실제 사용 시:
            // runBlocking {
            //     val client = ElasticsearchClientWrapper.create()
            //     val response = client.searchAsync<Product> { ... }
            //     client.close()
            // }
        }

        test("문서 인덱싱 예제") {
            // 실제 사용 시:
            // val client = ElasticsearchClientWrapper.create()
            // val product = Product(...)
            // val docId = client.index("products", product, product.id)
            // client.close()
        }

        test("대량 인덱싱 예제") {
            // 실제 사용 시:
            // val client = ElasticsearchClientWrapper.create()
            // val products = listOf(...)
            // val result = client.bulkIndex("products", products) { it.id }
            // client.close()
        }

        test("인덱스 관리 예제") {
            // 실제 사용 시:
            // val client = ElasticsearchClientWrapper.create()
            // if (!client.indexExists("products")) {
            //     client.createIndex("products", mappings, settings)
            // }
            // val health = client.health()
            // client.close()
        }
    }

    context("설정별 사용 시나리오") {
        
        test("개발 환경 설정") {
            val devConfig = ElasticsearchClientConfig.local()
            devConfig.hosts shouldBe listOf("localhost:9200")
            devConfig.protocol shouldBe "http"
            devConfig.enableSsl shouldBe false
        }

        test("스테이징 환경 설정") {
            val stagingConfig = ElasticsearchClientConfig(
                hosts = listOf("staging-es:9200"),
                protocol = "https",
                username = "staging_user",
                password = "staging_pass",
                enableSsl = true,
                sslVerificationEnabled = false // 자체 서명 인증서
            )
            
            stagingConfig.protocol shouldBe "https"
            stagingConfig.enableSsl shouldBe true
            stagingConfig.sslVerificationEnabled shouldBe false
        }

        test("운영 환경 설정") {
            val prodConfig = ElasticsearchClientConfig.production(
                hosts = listOf(
                    "es-master-1.prod.com:9200",
                    "es-master-2.prod.com:9200",
                    "es-master-3.prod.com:9200"
                ),
                username = "prod_user", 
                password = "secure_password"
            )
            
            prodConfig.hosts.size shouldBe 3
            prodConfig.protocol shouldBe "https"
            prodConfig.enableSsl shouldBe true
            prodConfig.sslVerificationEnabled shouldBe true
        }
    }
})