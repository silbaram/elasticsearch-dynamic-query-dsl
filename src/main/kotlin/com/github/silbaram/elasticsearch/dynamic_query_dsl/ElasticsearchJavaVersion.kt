package com.github.silbaram.elasticsearch.dynamic_query_dsl

import co.elastic.clients.elasticsearch._types.query_dsl.Query

/**
 * Utility object that exposes the version of the Elasticsearch Java client
 * present on the classpath.  The value is resolved from the package
 * implementation version so that the DSL can adapt behaviour depending on
 * the client it is compiled against.
 */
object ElasticsearchJavaVersion {
    /**
     * The full semantic version (e.g. `8.9.1`).
     */
    val version: String by lazy {
        Query::class.java.`package`.implementationVersion ?: "unknown"
    }

    /**
     * Convenience accessor for the major version as an `Int`, or `null` when
     * the version cannot be determined.
     */
    val major: Int? by lazy {
        version.split('.').firstOrNull()?.toIntOrNull()
    }
}
