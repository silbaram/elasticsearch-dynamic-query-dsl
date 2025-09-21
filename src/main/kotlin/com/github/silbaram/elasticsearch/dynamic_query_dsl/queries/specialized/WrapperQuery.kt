package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Base64

class WrapperQueryDsl {
    var boost: Float? = null
    var _name: String? = null

    private var encodedQuery: String? = null
    private var rawPayload: ByteArray? = null

    var query: String?
        get() = encodedQuery
        set(value) {
            base64(value)
        }

    /**
     * Set the wrapper query with a base64 encoded payload.
     */
    fun base64(value: String?) {
        encodedQuery = value
        if (!value.isNullOrBlank()) {
            rawPayload = null
        }
    }

    /**
     * Provide raw JSON (or other encoded string) and have it base64 encoded automatically.
     */
    fun rawJson(json: String, charset: Charset = StandardCharsets.UTF_8) {
        if (json.isBlank()) {
            rawPayload = null
            encodedQuery = null
            return
        }
        rawPayload = json.toByteArray(charset)
        encodedQuery = null
    }

    /**
     * Provide raw bytes that will be base64 encoded.
     */
    fun rawBytes(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            rawPayload = null
            encodedQuery = null
            return
        }
        rawPayload = bytes.copyOf()
        encodedQuery = null
    }

    internal fun buildEncodedQuery(): String? {
        rawPayload?.takeIf { it.isNotEmpty() }?.let { bytes ->
            return Base64.getEncoder().encodeToString(bytes)
        }
        return encodedQuery?.takeIf { it.isNotBlank() }
    }
}

fun Query.Builder.wrapperQuery(fn: WrapperQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = WrapperQueryDsl().apply(fn)
    val encoded = dsl.buildEncodedQuery() ?: return this

    return this.wrapper { wrapper ->
        wrapper.query(encoded)
        dsl.boost?.let { wrapper.boost(it) }
        dsl._name?.let { wrapper.queryName(it) }
        wrapper
    }
}
