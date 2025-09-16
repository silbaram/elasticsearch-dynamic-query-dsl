package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span

import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.SpanContainingQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanWithinQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanOrQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanNearQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanTermQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanFieldMaskingQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanNotQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanFirstQuery as EsSpanFirstQuery
import co.elastic.clients.elasticsearch._types.query_dsl.SpanMultiTermQuery
import co.elastic.clients.util.ObjectBuilder
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.SubQueryBuilders
import com.github.silbaram.elasticsearch.dynamic_query_dsl.core.queryOrNull

/**
 * Converts a generic Query object to a specific SpanQuery object, if it is a span query variant.
 * This is necessary because span container queries (like span_containing) require SpanQuery inputs,
 * but for DSL consistency, all query builder functions should return the generic Query type.
 */
private fun Query.toSpanQuery(): SpanQuery? {
    return when (this._kind()) {
        Query.Kind.SpanTerm -> SpanQuery.of { s -> s.spanTerm(this.spanTerm()) }
        Query.Kind.SpanNear -> SpanQuery.of { s -> s.spanNear(this.spanNear()) }
        Query.Kind.SpanContaining -> SpanQuery.of { s -> s.spanContaining(this.spanContaining()) }
        Query.Kind.SpanFirst -> SpanQuery.of { s -> s.spanFirst(this.spanFirst()) }
        Query.Kind.SpanNot -> SpanQuery.of { s -> s.spanNot(this.spanNot()) }
        Query.Kind.SpanOr -> SpanQuery.of { s -> s.spanOr(this.spanOr()) }
        Query.Kind.SpanWithin -> SpanQuery.of { s -> s.spanWithin(this.spanWithin()) }
        Query.Kind.SpanMulti -> SpanQuery.of { s -> s.spanMulti(this.spanMulti()) }
        // Support span_field_masking as a span query variant if available.
        Query.Kind.SpanFieldMasking -> SpanQuery.of { s -> s.spanFieldMasking(this.spanFieldMasking()) }
        else -> null
    }
}

/**
 * Build a span_term as Query
 */
@Deprecated(
    message = "함수형 빌더는 deprecated입니다. DSL을 사용하세요: query { spanTermQuery { ... } }",
    replaceWith = ReplaceWith(
        expression = "query { spanTermQuery { /* field=..., value=... */ } }",
        imports = [
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query",
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span.spanTermQuery"
        ]
    )
)
fun spanTermQuery(
    field: String,
    value: String?,
    boost: Float? = null,
    _name: String? = null
): Query? {
    if (field.isBlank() || value.isNullOrBlank()) return null

    val builder = SpanTermQuery.Builder()
        .field(field)
        .value(value)

    boost?.let { builder.boost(it) }
    _name?.let { builder.queryName(it) }

    return builder.build()._toQuery()
}

/**
 * Span Term Query DSL 클래스
 * query { spanTermQuery { ... } } 형태로 사용 가능
 */
class SpanTermQueryDsl {
    var field: String? = null
    var value: String? = null
    var boost: Float? = null
    var _name: String? = null
}

/**
 * Query.Builder를 위한 spanTermQuery DSL 확장 함수
 */
fun Query.Builder.spanTermQuery(fn: SpanTermQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = SpanTermQueryDsl().apply(fn)
    val field = dsl.field?.takeIf { it.isNotBlank() }
    val value = dsl.value?.takeIf { it.isNotBlank() }

    return if (field != null && value != null) {
        this.spanTerm { st ->
            st.field(field)
            st.value(value)
            dsl.boost?.let { st.boost(it) }
            dsl._name?.let { st.queryName(it) }
            st
        }
    } else {
        this // no-op when invalid inputs
    }
}

/**
 * Build a span_near as Query
 */
@Deprecated(
    message = "함수형 빌더는 deprecated입니다. DSL을 사용하세요: query { spanNearQuery { ... } }",
    replaceWith = ReplaceWith(
        expression = "query { spanNearQuery { /* slop=..., clauses[...] */ } }",
        imports = [
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query",
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span.spanNearQuery"
        ]
    )
)
fun spanNearQuery(
    clauses: List<Query?>?,
    slop: Int,
    inOrder: Boolean? = null,
    boost: Float? = null,
    _name: String? = null
): Query? {
    val validSpanClauses = clauses?.mapNotNull { it?.toSpanQuery() }.orEmpty()
    if (validSpanClauses.isEmpty() || slop < 0) return null

    val builder = SpanNearQuery.Builder()
        .slop(slop)
        .clauses(validSpanClauses)

    inOrder?.let { builder.inOrder(it) }
    boost?.let { builder.boost(it) }
    _name?.let { builder.queryName(it) }

    return builder.build()._toQuery()
}

/**
 * Build a span_field_masking as Query
 */
@Deprecated(
    message = "함수형 빌더는 deprecated입니다. DSL을 사용하세요: query { spanFieldMaskingQuery { ... } }",
    replaceWith = ReplaceWith(
        expression = "query { spanFieldMaskingQuery { /* query{...}; field=... */ } }",
        imports = [
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query",
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span.spanFieldMaskingQuery"
        ]
    )
)
fun spanFieldMaskingQuery(
    query: Query?,
    field: String?,
    boost: Float? = null,
    _name: String? = null
): Query? {
    val span = query?.toSpanQuery() ?: return null
    val targetField = field?.takeIf { it.isNotBlank() } ?: return null

    val builder = SpanFieldMaskingQuery.Builder()
        .query(span)
        .field(targetField)

    boost?.let { builder.boost(it) }
    _name?.let { builder.queryName(it) }

    return builder.build()._toQuery()
}

/**
 * Build a span_multi as Query
 */
@Deprecated(
    message = "함수형 빌더는 deprecated입니다. DSL을 사용하세요: query { spanMultiQuery { ... } }",
    replaceWith = ReplaceWith(
        expression = "query { spanMultiQuery { /* match{...} */ } }",
        imports = [
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query",
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span.spanMultiQuery"
        ]
    )
)
fun spanMultiQuery(
    match: Query?,
    boost: Float? = null,
    _name: String? = null
): Query? {
    val m = match ?: return null
    // Allow only multi-term kinds: prefix, wildcard, regexp, fuzzy, range
    val isMultiTerm = when (m._kind()) {
        Query.Kind.Prefix,
        Query.Kind.Wildcard,
        Query.Kind.Regexp,
        Query.Kind.Fuzzy,
        Query.Kind.Range -> true
        else -> false
    }
    if (!isMultiTerm) return null

    val builder = SpanMultiTermQuery.Builder()
        .match(m)

    boost?.let { builder.boost(it) }
    _name?.let { builder.queryName(it) }

    return builder.build()._toQuery()
}

/**
 * Build a span_containing top-level Query from two Query inputs.
 * The inputs must be variants of span queries.
 * Returns null when either input is null or not a valid span query.
 */
@Deprecated(
    message = "함수형 빌더는 deprecated입니다. DSL을 사용하세요: query { spanContainingQuery { ... } }",
    replaceWith = ReplaceWith(
        expression = "query { spanContainingQuery { /* little{...}; big{...} */ } }",
        imports = [
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query",
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span.spanContainingQuery"
        ]
    )
)
fun spanContainingQuery(
    little: Query?,
    big: Query?,
    boost: Float? = null,
    _name: String? = null
): Query? {
    val littleSpan = little?.toSpanQuery()
    val bigSpan = big?.toSpanQuery()

    if (littleSpan == null || bigSpan == null) return null

    val builder = SpanContainingQuery.Builder()
        .little(littleSpan)
        .big(bigSpan)

    boost?.let { builder.boost(it) }
    _name?.let { builder.queryName(it) }

    return builder.build()._toQuery()
}

/**
 * Build a span_within top-level Query from two Query inputs.
 * The inputs must be variants of span queries.
 * Returns null when either input is null or not a valid span query.
 */
@Deprecated(
    message = "함수형 빌더는 deprecated입니다. DSL을 사용하세요: query { spanWithinQuery { ... } }",
    replaceWith = ReplaceWith(
        expression = "query { spanWithinQuery { /* little{...}; big{...} */ } }",
        imports = [
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query",
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span.spanWithinQuery"
        ]
    )
)
fun spanWithinQuery(
    little: Query?,
    big: Query?,
    boost: Float? = null,
    _name: String? = null
): Query? {
    val littleSpan = little?.toSpanQuery()
    val bigSpan = big?.toSpanQuery()

    if (littleSpan == null || bigSpan == null) return null

    val builder = SpanWithinQuery.Builder()
        .little(littleSpan)
        .big(bigSpan)

    boost?.let { builder.boost(it) }
    _name?.let { builder.queryName(it) }

    return builder.build()._toQuery()
}

/**
 * Build a span_or as Query
 */
@Deprecated(
    message = "함수형 빌더는 deprecated입니다. DSL을 사용하세요: query { spanOrQuery { ... } }",
    replaceWith = ReplaceWith(
        expression = "query { spanOrQuery { /* clauses[...] */ } }",
        imports = [
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query",
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span.spanOrQuery"
        ]
    )
)
fun spanOrQuery(
    clauses: List<Query?>?,
    boost: Float? = null,
    _name: String? = null
): Query? {
    val validClauses = clauses?.mapNotNull { it?.toSpanQuery() }.orEmpty()
    if (validClauses.isEmpty()) return null

    val builder = SpanOrQuery.Builder()
        .clauses(validClauses)

    boost?.let { builder.boost(it) }
    _name?.let { builder.queryName(it) }

    return builder.build()._toQuery()
}

/**
 * Span Containing Query DSL 클래스
 * query { spanContainingQuery { ... } } 형태로 사용 가능
 */
class SpanContainingQueryDsl {
    internal val littleBuilders = SubQueryBuilders()
    internal val bigBuilders = SubQueryBuilders()
    var boost: Float? = null
    var _name: String? = null

    fun little(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) sub.addQuery(result)
        littleBuilders.addAll(sub)
    }

    fun big(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) sub.addQuery(result)
        bigBuilders.addAll(sub)
    }
}

/**
 * Query.Builder를 위한 spanContainingQuery DSL 확장 함수
 */
fun Query.Builder.spanContainingQuery(fn: SpanContainingQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = SpanContainingQueryDsl().apply(fn)

    val little = when (dsl.littleBuilders.size()) {
        0 -> null
        1 -> { var r: Query? = null; dsl.littleBuilders.forEach { r = it }; r }
        else -> null
    }
    val big = when (dsl.bigBuilders.size()) {
        0 -> null
        1 -> { var r: Query? = null; dsl.bigBuilders.forEach { r = it }; r }
        else -> null
    }
    val littleSpan = little?.toSpanQuery()
    val bigSpan = big?.toSpanQuery()
    if (littleSpan == null || bigSpan == null) return this

    return this.spanContaining { sc ->
        sc.little(littleSpan)
        sc.big(bigSpan)
        dsl.boost?.let { sc.boost(it) }
        dsl._name?.let { sc.queryName(it) }
        sc
    }
}

/**
 * Span First Query DSL 클래스
 * query { spanFirstQuery { ... } } 형태로 사용 가능
 */
class SpanFirstQueryDsl {
    internal val matchBuilders = SubQueryBuilders()
    var match: Query? = null
    var end: Int? = null
    var boost: Float? = null
    var _name: String? = null

    fun match(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) sub.addQuery(result)
        matchBuilders.addAll(sub)
    }
}

/**
 * Query.Builder를 위한 spanFirstQuery DSL 확장 함수
 */
fun Query.Builder.spanFirstQueryDsl(fn: SpanFirstQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = SpanFirstQueryDsl().apply(fn)

    val matchFromBuilders = when (dsl.matchBuilders.size()) {
        0 -> null
        1 -> { var r: Query? = null; dsl.matchBuilders.forEach { r = it }; r }
        else -> null
    }
    val matchQuery = matchFromBuilders ?: dsl.match
    val span = matchQuery?.toSpanQuery() ?: return this
    val end = dsl.end ?: return this

    return this.spanFirst { sf ->
        sf.match(span)
        sf.end(end)
        dsl.boost?.let { sf.boost(it) }
        dsl._name?.let { sf.queryName(it) }
        sf
    }
}

/**
 * Build a span_not as Query
 */
@Deprecated(
    message = "함수형 빌더는 deprecated입니다. DSL을 사용하세요: query { spanNotQuery { ... } }",
    replaceWith = ReplaceWith(
        expression = "query { spanNotQuery { /* include{...}; exclude{...} */ } }",
        imports = [
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.core.query",
            "com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span.spanNotQuery"
        ]
    )
)
fun spanNotQuery(
    include: Query?,
    exclude: Query?,
    pre: Int? = null,
    post: Int? = null,
    boost: Float? = null,
    _name: String? = null
): Query? {
    val inc = include?.toSpanQuery() ?: return null
    val exc = exclude?.toSpanQuery() ?: return null

    val builder = SpanNotQuery.Builder()
        .include(inc)
        .exclude(exc)

    pre?.takeIf { it >= 0 }?.let { builder.pre(it) }
    post?.takeIf { it >= 0 }?.let { builder.post(it) }
    boost?.let { builder.boost(it) }
    _name?.let { builder.queryName(it) }

    return builder.build()._toQuery()
}

/**
 * Span Field Masking Query DSL 클래스
 * query { spanFieldMaskingQuery { ... } } 형태로 사용 가능
 */
class SpanFieldMaskingQueryDsl {
    internal val queryBuilders = SubQueryBuilders()
    var field: String? = null
    var boost: Float? = null
    var _name: String? = null

    /**
     * 마스킹할 스팬 쿼리 설정
     */
    fun query(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) {
            sub.addQuery(result)
        }
        queryBuilders.addAll(sub)
    }

    internal fun buildQuery(): Query? {
        val spanQuery = when (queryBuilders.size()) {
            0 -> return null
            1 -> {
                var result: Query? = null
                queryBuilders.forEach { result = it }
                result
            }
            else -> return null // span_field_masking은 단일 쿼리만 지원
        }

        // Deprecated 함수 호출 대신 직접 빌더 사용
        val span = spanQuery?.toSpanQuery() ?: return null
        val targetField = field?.takeIf { it.isNotBlank() } ?: return null

        val builder = SpanFieldMaskingQuery.Builder()
            .query(span)
            .field(targetField)

        boost?.let { builder.boost(it) }
        _name?.let { builder.queryName(it) }

        return builder.build()._toQuery()
    }
}

/**
 * Query.Builder를 위한 spanFieldMaskingQuery DSL 확장 함수
 * query { spanFieldMaskingQuery { ... } } 형태로 사용 가능
 */
fun Query.Builder.spanFieldMaskingQuery(fn: SpanFieldMaskingQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = SpanFieldMaskingQueryDsl().apply(fn)
    
    // 입력된 쿼리와 필드 유효성 확인
    val query = when (dsl.queryBuilders.size()) {
        0 -> return this // 쿼리가 없으면 no-op
        1 -> {
            var result: Query? = null
            dsl.queryBuilders.forEach { result = it }
            result
        }
        else -> return this // 여러 쿼리는 지원하지 않음
    }
    
    val spanQuery = query?.toSpanQuery()
    val field = dsl.field?.takeIf { it.isNotBlank() }
    
    return if (spanQuery != null && field != null) {
        this.spanFieldMasking { sfm ->
            sfm.query(spanQuery)
            sfm.field(field)
            dsl.boost?.let { sfm.boost(it) }
            dsl._name?.let { sfm.queryName(it) }
            sfm
        }
    } else {
        this // no-op if invalid inputs
    }
}

/**
 * Span Multi Query DSL 클래스
 * query { spanMultiQuery { ... } } 형태로 사용 가능
 */
class SpanMultiQueryDsl {
    internal val matchBuilders = SubQueryBuilders()
    var boost: Float? = null
    var _name: String? = null

    /**
     * 멀티텀(match) 쿼리 설정. 단일 쿼리만 허용.
     */
    fun match(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) {
            sub.addQuery(result)
        }
        matchBuilders.addAll(sub)
    }

    internal fun buildQuery(): Query? {
        val match = when (matchBuilders.size()) {
            0 -> return null
            1 -> {
                var result: Query? = null
                matchBuilders.forEach { result = it }
                result
            }
            else -> return null // span_multi는 단일 match만 지원
        }

        // Deprecated 함수 호출 대신 직접 빌더 사용 (멀티텀만 허용)
        val m = match ?: return null
        val isMultiTerm = when (m._kind()) {
            Query.Kind.Prefix,
            Query.Kind.Wildcard,
            Query.Kind.Regexp,
            Query.Kind.Fuzzy,
            Query.Kind.Range -> true
            else -> false
        }
        if (!isMultiTerm) return null

        val builder = SpanMultiTermQuery.Builder()
            .match(m)

        boost?.let { builder.boost(it) }
        _name?.let { builder.queryName(it) }

        return builder.build()._toQuery()
    }
}

/**
 * Query.Builder를 위한 spanMultiQuery DSL 확장 함수
 * query { spanMultiQuery { ... } } 형태로 사용 가능
 */
fun Query.Builder.spanMultiQuery(fn: SpanMultiQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = SpanMultiQueryDsl().apply(fn)

    val match = when (dsl.matchBuilders.size()) {
        0 -> return this // no-op: match 미제공
        1 -> {
            var result: Query? = null
            dsl.matchBuilders.forEach { result = it }
            result
        }
        else -> return this // 여러 match는 지원하지 않음
    }

    // 멀티텀 검증
    val m = match ?: return this
    val isMultiTerm = when (m._kind()) {
        Query.Kind.Prefix,
        Query.Kind.Wildcard,
        Query.Kind.Regexp,
        Query.Kind.Fuzzy,
        Query.Kind.Range -> true
        else -> false
    }
    if (!isMultiTerm) return this

    return this.spanMulti { sm ->
        sm.match(m)
        dsl.boost?.let { sm.boost(it) }
        dsl._name?.let { sm.queryName(it) }
        sm
    }
}

/**
 * Array-style DSL을 위한 clauses 헬퍼 클래스
 */
class ClausesArrayHelper(private val clauseBuilders: MutableList<Query?>) {
    operator fun get(vararg queries: Query?): Unit {
        clauseBuilders.clear()
        queries.filterNotNull().forEach { clauseBuilders.add(it) }
    }

    // Support builder lambdas inside clauses[ { ... }, { ... } ]
    operator fun get(vararg builders: Query.Builder.() -> Unit) {
        clauseBuilders.clear()
        builders.forEach { b -> clauseBuilders.add(queryOrNull(b)) }
    }
}

/**
 * Span Near Query DSL 클래스
 * query { spanNearQuery { ... } } 형태로 사용 가능
 */
class SpanNearQueryDsl {
    private val clauseBuilders = mutableListOf<Query?>()
    var slop: Int? = null
    var inOrder: Boolean? = null
    var boost: Float? = null
    var _name: String? = null
    
    // Array-style DSL - clauses[query1, query2, ...] 형태로 사용 가능
    val clauses = ClausesArrayHelper(clauseBuilders)
    
    // DSL 방식 - clause 블록으로 쿼리 추가
    fun clause(query: Query?) {
        query?.let { clauseBuilders.add(it) }
    }
    
    internal fun getClauses(): List<Query?> = clauseBuilders.toList()
}

/**
 * Query.Builder를 위한 spanNearQuery DSL 확장 함수
 * query { spanNearQuery { ... } } 형태로 사용 가능
 */
fun Query.Builder.spanNearQuery(fn: SpanNearQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = SpanNearQueryDsl().apply(fn)
    
    val clauses = dsl.getClauses()
    val slop = dsl.slop
    
    if (clauses.isEmpty() || slop == null || slop < 0) {
        return this // no-op if invalid inputs
    }
    
    val validSpanClauses = clauses.mapNotNull { it?.toSpanQuery() }
    if (validSpanClauses.isEmpty()) {
        return this // no-op if no valid span clauses
    }
    
    return this.spanNear { snq ->
        snq.clauses(validSpanClauses)
        snq.slop(slop)
        dsl.inOrder?.let { snq.inOrder(it) }
        dsl.boost?.let { snq.boost(it) }
        dsl._name?.let { snq.queryName(it) }
        snq
    }
}

/**
 * Span Or Query DSL 클래스
 * query { spanOrQuery { ... } } 형태로 사용 가능
 */
class SpanOrQueryDsl {
    private val clauseBuilders = mutableListOf<Query?>()
    var boost: Float? = null
    var _name: String? = null

    val clauses = ClausesArrayHelper(clauseBuilders)

    fun clause(query: Query?) {
        query?.let { clauseBuilders.add(it) }
    }

    internal fun getClauses(): List<Query?> = clauseBuilders.toList()
}

/**
 * Query.Builder를 위한 spanOrQuery DSL 확장 함수
 */
fun Query.Builder.spanOrQuery(fn: SpanOrQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = SpanOrQueryDsl().apply(fn)
    val clauses = dsl.getClauses()
    if (clauses.isEmpty()) return this

    val valid = clauses.mapNotNull { it?.toSpanQuery() }
    if (valid.isEmpty()) return this

    return this.spanOr { soq ->
        soq.clauses(valid)
        dsl.boost?.let { soq.boost(it) }
        dsl._name?.let { soq.queryName(it) }
        soq
    }
}

/**
 * Span Within Query DSL 클래스
 * query { spanWithinQuery { ... } } 형태로 사용 가능
 */
class SpanWithinQueryDsl {
    internal val littleBuilders = SubQueryBuilders()
    internal val bigBuilders = SubQueryBuilders()
    var boost: Float? = null
    var _name: String? = null

    fun little(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) {
            sub.addQuery(result)
        }
        littleBuilders.addAll(sub)
    }

    fun big(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) {
            sub.addQuery(result)
        }
        bigBuilders.addAll(sub)
    }
}

/**
 * Query.Builder를 위한 spanWithinQuery DSL 확장 함수
 */
fun Query.Builder.spanWithinQuery(fn: SpanWithinQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = SpanWithinQueryDsl().apply(fn)

    val little = when (dsl.littleBuilders.size()) {
        0 -> null
        1 -> {
            var result: Query? = null
            dsl.littleBuilders.forEach { result = it }
            result
        }
        else -> null
    }
    val big = when (dsl.bigBuilders.size()) {
        0 -> null
        1 -> {
            var result: Query? = null
            dsl.bigBuilders.forEach { result = it }
            result
        }
        else -> null
    }

    val littleSpan = little?.toSpanQuery()
    val bigSpan = big?.toSpanQuery()
    if (littleSpan == null || bigSpan == null) return this

    return this.spanWithin { swq ->
        swq.little(littleSpan)
        swq.big(bigSpan)
        dsl.boost?.let { swq.boost(it) }
        dsl._name?.let { swq.queryName(it) }
        swq
    }
}

/**
 * Span Not Query DSL 클래스
 * query { spanNotQuery { ... } } 형태로 사용 가능
 */
class SpanNotQueryDsl {
    internal val includeBuilders = SubQueryBuilders()
    internal val excludeBuilders = SubQueryBuilders()
    var pre: Int? = null
    var post: Int? = null
    var boost: Float? = null
    var _name: String? = null

    fun include(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) {
            sub.addQuery(result)
        }
        includeBuilders.addAll(sub)
    }

    fun exclude(fn: SubQueryBuilders.() -> Any?) {
        val sub = SubQueryBuilders()
        val result = sub.fn()
        if (sub.size() == 0 && result is Query) {
            sub.addQuery(result)
        }
        excludeBuilders.addAll(sub)
    }
}

/**
 * Query.Builder를 위한 spanNotQuery DSL 확장 함수
 * query { spanNotQuery { ... } } 형태로 사용 가능
 */
fun Query.Builder.spanNotQuery(fn: SpanNotQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = SpanNotQueryDsl().apply(fn)

    val includeQuery = when (dsl.includeBuilders.size()) {
        0 -> null
        1 -> {
            var result: Query? = null
            dsl.includeBuilders.forEach { result = it }
            result
        }
        else -> null // 단일만 허용
    }

    val excludeQuery = when (dsl.excludeBuilders.size()) {
        0 -> null
        1 -> {
            var result: Query? = null
            dsl.excludeBuilders.forEach { result = it }
            result
        }
        else -> null // 단일만 허용
    }

    val inc = includeQuery?.toSpanQuery()
    val exc = excludeQuery?.toSpanQuery()
    if (inc == null || exc == null) return this // no-op

    return this.spanNot { sn ->
        sn.include(inc)
        sn.exclude(exc)
        dsl.pre?.takeIf { it >= 0 }?.let { sn.pre(it) }
        dsl.post?.takeIf { it >= 0 }?.let { sn.post(it) }
        dsl.boost?.let { sn.boost(it) }
        dsl._name?.let { sn.queryName(it) }
        sn
    }
}
