package com.github.silbaram.elasticsearch.dynamic_query_dsl

import co.elastic.clients.elasticsearch.ElasticsearchClient
import java.util.Properties

fun elasticsearchClientVersion(): String? =
    ElasticsearchClient::class.java.classLoader
        ?.getResourceAsStream("META-INF/maven/co.elastic.clients/elasticsearch-java/pom.properties")
        ?.use { input ->
            Properties().apply { load(input) }.getProperty("version")
        }
