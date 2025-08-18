package com.github.silbaram.elasticsearch.dynamic_query_dsl

import co.elastic.clients.elasticsearch.ElasticsearchClient

fun elasticsearchClientVersion(): String? {
    return ElasticsearchClient::class.java.`package`?.implementationVersion
}
