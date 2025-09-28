package com.github.silbaram.elasticsearch.dynamic_query_dsl.client

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.SecureRandom
import java.security.cert.X509Certificate

/**
 * Elasticsearch 클라이언트 설정 클래스
 */
data class ElasticsearchClientConfig(
    val hosts: List<String> = listOf("localhost:9200"),
    val protocol: String = "http",
    val username: String? = null,
    val password: String? = null,
    val connectTimeoutMillis: Int = 5000,
    val socketTimeoutMillis: Int = 60000,
    val enableSsl: Boolean = false,
    val sslVerificationEnabled: Boolean = true,
    val maxRetryTimeoutMillis: Int = 60000
) {
    companion object {
        /**
         * 로컬 개발용 기본 설정
         */
        fun local(): ElasticsearchClientConfig = ElasticsearchClientConfig()

        /**
         * 운영 환경용 설정
         */
        fun production(
            hosts: List<String>,
            username: String?,
            password: String?
        ): ElasticsearchClientConfig = ElasticsearchClientConfig(
            hosts = hosts,
            protocol = "https",
            username = username,
            password = password,
            enableSsl = true,
            sslVerificationEnabled = true
        )

        /**
         * 환경변수에서 설정 로드
         */
        fun fromEnvironment(): ElasticsearchClientConfig {
            val hostsEnv = System.getenv("ELASTICSEARCH_HOSTS")
            val hosts = if (hostsEnv != null) {
                hostsEnv.split(",").map { it.trim() }
            } else {
                listOf("localhost:9200")
            }

            return ElasticsearchClientConfig(
                hosts = hosts,
                protocol = System.getenv("ELASTICSEARCH_PROTOCOL") ?: "http",
                username = System.getenv("ELASTICSEARCH_USERNAME"),
                password = System.getenv("ELASTICSEARCH_PASSWORD"),
                connectTimeoutMillis = System.getenv("ELASTICSEARCH_CONNECT_TIMEOUT")?.toIntOrNull() ?: 5000,
                socketTimeoutMillis = System.getenv("ELASTICSEARCH_SOCKET_TIMEOUT")?.toIntOrNull() ?: 60000,
                enableSsl = System.getenv("ELASTICSEARCH_SSL_ENABLED")?.toBoolean() ?: false,
                sslVerificationEnabled = System.getenv("ELASTICSEARCH_SSL_VERIFICATION")?.toBoolean() ?: true,
                maxRetryTimeoutMillis = System.getenv("ELASTICSEARCH_MAX_RETRY_TIMEOUT")?.toIntOrNull() ?: 60000
            )
        }
    }

    /**
     * Elasticsearch 클라이언트 생성
     */
    fun createClient(): ElasticsearchClient {
        val httpHosts = hosts.map { host ->
            val parts = host.split(":")
            val hostname = parts[0]
            val port = if (parts.size > 1) parts[1].toInt() else if (protocol == "https") 443 else 9200
            HttpHost(hostname, port, protocol)
        }.toTypedArray()

        val restClientBuilder = RestClient.builder(*httpHosts).apply {
            setRequestConfigCallback { requestConfigBuilder ->
                requestConfigBuilder
                    .setConnectTimeout(connectTimeoutMillis)
                    .setConnectionRequestTimeout(maxRetryTimeoutMillis)
                    .setSocketTimeout(socketTimeoutMillis)
            }
            setHttpClientConfigCallback { httpClientBuilder ->
                applySsl(applyAuthentication(httpClientBuilder))
            }
        }

        val restClient = restClientBuilder.build()
        val transport = RestClientTransport(restClient, JacksonJsonpMapper())
        return ElasticsearchClient(transport)
    }

    private fun applyAuthentication(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder {
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            return httpClientBuilder
        }

        val credentialsProvider: CredentialsProvider = BasicCredentialsProvider().apply {
            setCredentials(AuthScope.ANY, UsernamePasswordCredentials(username, password))
        }

        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        return httpClientBuilder
    }

    private fun applySsl(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder {
        if (!enableSsl) {
            return httpClientBuilder
        }

        if (!sslVerificationEnabled) {
            httpClientBuilder
                .setSSLHostnameVerifier { _, _ -> true }
                .setSSLContext(createTrustAllSslContext())
        }

        return httpClientBuilder
    }

    private fun createTrustAllSslContext(): SSLContext {
        val trustAllManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) = Unit
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = emptyArray()
        }

        return SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(trustAllManager), SecureRandom())
        }
    }
}
