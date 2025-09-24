package com.github.silbaram.elasticsearch.dynamic_query_dsl.core

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.termlevel.*
import co.elastic.clients.elasticsearch._types.query_dsl.CombinedFieldsOperator
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.span.*
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
import co.elastic.clients.elasticsearch._types.query_dsl.SimpleQueryStringFlag
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.specialized.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.joining.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.compound.*
import com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.geo.*

/**
 * 여러 clause 확장함수에서 재사용하는 Query 수집기.
 * - 기존 `queries[...]` 구문을 지원
 * - 단일 쿼리 추가용 addQuery / unaryPlus 지원
 */
class SubQueryBuilders {
    private val collectedQueries = mutableListOf<Query>()

    // queries[...] 사용을 위해 자기 자신 반환
    val queries: SubQueryBuilders
        get() = this

    // queries[ q1, q2 ] 를 처리
    operator fun get(vararg queries: Query?) {
        this.collectedQueries.addAll(queries.filterNotNull())
    }

    // queries[ { builder... }, { builder... } ] 형태 지원
    operator fun get(vararg builders: Query.Builder.() -> Unit) {
        builders.forEach { b -> addQuery(queryOrNull(b)) }
    }

    // 개별 쿼리 추가 (람다 안에서 호출할 때 사용)
    fun addQuery(query: Query?) {
        query?.let { collectedQueries.add(it) }
    }

    /**
     * 중첩된 bool 쿼리를 생성합니다. 이 함수는 mustQuery, shouldQuery 등 내부에서 호출되어야 합니다.
     * @param fn 중첩될 bool 쿼리를 설정하는 람다입니다.
     * @return 생성된 bool 쿼리를 담은 Query 객체를 반환합니다.
     */
    fun boolQuery(fn: BoolQuery.Builder.() -> Unit): Query {
        return Query.of { q ->
            q.bool { b ->
                b.fn()
                b
            }
        }
    }

    // + 쿼리 문법을 선호한다면 아래를 사용:
    operator fun Query?.unaryPlus() {
        this?.let { collectedQueries.add(it) }
    }

    fun size(): Int = collectedQueries.size

    fun forEach(action: (Query) -> Unit) = collectedQueries.forEach(action)

    fun addAll(other: SubQueryBuilders) {
        this.collectedQueries.addAll(other.collectedQueries)
    }

    // --- Inline helpers to avoid explicit `query { ... }` inside clause blocks ---
    fun termQuery(fn: TermQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.termQuery(fn) })
    }

    fun rangeQuery(fn: RangeQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.rangeQuery(fn) })
    }

    fun existsQuery(fn: ExistsQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.existsQuery(fn) })
    }

    fun prefixQuery(fn: PrefixQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.prefixQuery(fn) })
    }

    fun wildcardQuery(fn: WildcardQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.wildcardQuery(fn) })
    }

    fun regexpQuery(fn: RegexpQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.regexpQuery(fn) })
    }

    fun fuzzyQuery(fn: FuzzyQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.fuzzyQuery(fn) })
    }

    fun idsQuery(fn: IdsQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.idsQuery(fn) })
    }

    fun termsSetQuery(fn: TermsSetQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.termsSetQuery(fn) })
    }

    fun nestedQuery(fn: NestedQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.nestedQuery(fn) })
    }

    fun hasChildQuery(fn: HasChildQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.hasChildQuery(fn) })
    }

    fun hasParentQuery(fn: HasParentQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.hasParentQuery(fn) })
    }

    fun parentIdQuery(fn: ParentIdQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.parentIdQuery(fn) })
    }

    fun geoDistanceQuery(fn: GeoDistanceQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.geoDistanceQuery(fn) })
    }

    fun geoBoundingBoxQuery(fn: GeoBoundingBoxQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.geoBoundingBoxQuery(fn) })
    }

    fun geoShapeQuery(fn: GeoShapeQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.geoShapeQuery(fn) })
    }

    fun shapeQuery(fn: ShapeQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.shapeQuery(fn) })
    }

    fun disMaxQuery(fn: DisMaxQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.disMaxQuery(fn) })
    }

    fun matchNone(fn: MatchNoneDsl.() -> Unit = {}) {
        addQuery(queryOrNull { this.matchNoneQuery(fn) })
    }

    fun matchAll(fn: MatchAllDsl.() -> Unit = {}) {
        addQuery(queryOrNull { this.matchAllDsl(fn) })
    }

    fun matchQuery(fn: MatchQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.matchQuery(fn) })
    }

    fun spanTermQuery(fn: SpanTermQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.spanTermQuery(fn) })
    }

    fun spanContainingQuery(fn: SpanContainingQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.spanContainingQuery(fn) })
    }

    fun spanNearQuery(fn: SpanNearQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.spanNearQuery(fn) })
    }

    // fulltext intervals helper to allow direct use inside clause blocks
    fun intervals(field: String, boost: Float? = null, _name: String? = null, fn: IntervalsRuleDsl.() -> Unit) {
        addQuery(queryOrNull { this.intervals(field, boost, _name, fn) })
    }

    // combined_fields helper to allow direct call inside clause blocks
    fun combinedFields(
        query: String?,
        fields: List<String>,
        operator: CombinedFieldsOperator? = null,
        minimumShouldMatch: String? = null,
        autoGenerateSynonymsPhraseQuery: Boolean? = null,
        boost: Float? = null,
        _name: String? = null
    ) {
        addQuery(
            queryOrNull {
                this.combinedFields(
                    query = query,
                    fields = fields,
                    operator = operator,
                    minimumShouldMatch = minimumShouldMatch,
                    autoGenerateSynonymsPhraseQuery = autoGenerateSynonymsPhraseQuery,
                    boost = boost,
                    _name = _name
                )
            }
        )
    }

    // match_phrase_prefix helper to allow direct call inside clause blocks
    fun matchPhrasePrefix(
        field: String,
        query: String?,
        analyzer: String? = null,
        slop: Int? = null,
        zeroTermsQuery: ZeroTermsQuery? = null,
        maxExpansions: Int? = null,
        boost: Float? = null,
        _name: String? = null
    ) {
        addQuery(
            queryOrNull {
                this.matchPhrasePrefix(
                    field = field,
                    query = query,
                    analyzer = analyzer,
                    slop = slop,
                    zeroTermsQuery = zeroTermsQuery,
                    maxExpansions = maxExpansions,
                    boost = boost,
                    _name = _name
                )
            }
        )
    }

    // multi_match phrase helper
    fun multiMatchPhrase(
        query: String?,
        fields: List<String>,
        analyzer: String? = null,
        slop: Int? = null,
        zeroTermsQuery: ZeroTermsQuery? = null,
        boost: Float? = null,
        _name: String? = null
    ) {
        addQuery(
            queryOrNull {
                this.multiMatchPhrase(
                    query = query,
                    fields = fields,
                    analyzer = analyzer,
                    slop = slop,
                    zeroTermsQuery = zeroTermsQuery,
                    boost = boost,
                    _name = _name
                )
            }
        )
    }

    // multi_match helper (general)
    fun multiMatch(
        query: String?,
        fields: List<String>,
        type: TextQueryType? = null,
        operator: Operator? = null,
        minimumShouldMatch: String? = null,
        analyzer: String? = null,
        slop: Int? = null,
        tieBreaker: Double? = null,
        fuzziness: String? = null,
        prefixLength: Int? = null,
        maxExpansions: Int? = null,
        fuzzyTranspositions: Boolean? = null,
        fuzzyRewrite: String? = null,
        lenient: Boolean? = null,
        zeroTermsQuery: ZeroTermsQuery? = null,
        autoGenerateSynonymsPhraseQuery: Boolean? = null,
        boost: Float? = null,
        _name: String? = null
    ) {
        addQuery(
            queryOrNull {
                this.multiMatch(
                    query = query,
                    fields = fields,
                    type = type,
                    operator = operator,
                    minimumShouldMatch = minimumShouldMatch,
                    analyzer = analyzer,
                    slop = slop,
                    tieBreaker = tieBreaker,
                    fuzziness = fuzziness,
                    prefixLength = prefixLength,
                    maxExpansions = maxExpansions,
                    fuzzyTranspositions = fuzzyTranspositions,
                    fuzzyRewrite = fuzzyRewrite,
                    lenient = lenient,
                    zeroTermsQuery = zeroTermsQuery,
                    autoGenerateSynonymsPhraseQuery = autoGenerateSynonymsPhraseQuery,
                    boost = boost,
                    _name = _name
                )
            }
        )
    }

    // query_string helper
    fun queryString(
        query: String?,
        fields: List<String> = emptyList(),
        defaultField: String? = null,
        analyzer: String? = null,
        quoteAnalyzer: String? = null,
        quoteFieldSuffix: String? = null,
        defaultOperator: Operator? = null,
        allowLeadingWildcard: Boolean? = null,
        analyzeWildcard: Boolean? = null,
        autoGenerateSynonymsPhraseQuery: Boolean? = null,
        enablePositionIncrements: Boolean? = null,
        fuzziness: String? = null,
        fuzzyMaxExpansions: Int? = null,
        fuzzyPrefixLength: Int? = null,
        fuzzyTranspositions: Boolean? = null,
        lenient: Boolean? = null,
        minimumShouldMatch: String? = null,
        phraseSlop: Double? = null,
        boost: Float? = null,
        _name: String? = null
    ) {
        addQuery(
            queryOrNull {
                this.queryString(
                    query = query,
                    fields = fields,
                    defaultField = defaultField,
                    analyzer = analyzer,
                    quoteAnalyzer = quoteAnalyzer,
                    quoteFieldSuffix = quoteFieldSuffix,
                    defaultOperator = defaultOperator,
                    allowLeadingWildcard = allowLeadingWildcard,
                    analyzeWildcard = analyzeWildcard,
                    autoGenerateSynonymsPhraseQuery = autoGenerateSynonymsPhraseQuery,
                    enablePositionIncrements = enablePositionIncrements,
                    fuzziness = fuzziness,
                    fuzzyMaxExpansions = fuzzyMaxExpansions,
                    fuzzyPrefixLength = fuzzyPrefixLength,
                    fuzzyTranspositions = fuzzyTranspositions,
                    lenient = lenient,
                    minimumShouldMatch = minimumShouldMatch,
                    phraseSlop = phraseSlop,
                    boost = boost,
                    _name = _name
                )
            }
        )
    }

    // simple_query_string helper
    fun simpleQueryString(
        query: String?,
        fields: List<String> = emptyList(),
        defaultOperator: Operator? = null,
        analyzer: String? = null,
        quoteFieldSuffix: String? = null,
        analyzeWildcard: Boolean? = null,
        flags: List<SimpleQueryStringFlag> = emptyList(),
        fuzzyMaxExpansions: Int? = null,
        fuzzyPrefixLength: Int? = null,
        fuzzyTranspositions: Boolean? = null,
        autoGenerateSynonymsPhraseQuery: Boolean? = null,
        minimumShouldMatch: String? = null,
        lenient: Boolean? = null,
        boost: Float? = null,
        _name: String? = null
    ) {
        addQuery(
            queryOrNull {
                this.simpleQueryString(
                    query = query,
                    fields = fields,
                    defaultOperator = defaultOperator,
                    analyzer = analyzer,
                    quoteFieldSuffix = quoteFieldSuffix,
                    analyzeWildcard = analyzeWildcard,
                    flags = flags,
                    fuzzyMaxExpansions = fuzzyMaxExpansions,
                    fuzzyPrefixLength = fuzzyPrefixLength,
                    fuzzyTranspositions = fuzzyTranspositions,
                    autoGenerateSynonymsPhraseQuery = autoGenerateSynonymsPhraseQuery,
                    minimumShouldMatch = minimumShouldMatch,
                    lenient = lenient,
                    boost = boost,
                    _name = _name
                )
            }
        )
    }

    // more_like_this helper
    fun moreLikeThis(fn: MoreLikeThisDsl.() -> Unit) {
        addQuery(
            queryOrNull {
                this.mlt(fn)
            }
        )
    }

    // script helper
    fun scriptQuery(fn: ScriptQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.scriptQuery(fn) })
    }

    // script_score helper
    fun scriptScoreQuery(fn: ScriptScoreQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.scriptScoreQuery(fn) })
    }

    // wrapper helper
    fun wrapperQuery(fn: WrapperQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.wrapperQuery(fn) })
    }

    // pinned helper
    fun pinnedQuery(fn: PinnedQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.pinnedQuery(fn) })
    }

    // rule helper
    fun ruleQuery(fn: RuleQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.ruleQueryDsl(fn) })
    }

    // weighted_tokens helper
    fun weightedTokensQuery(fn: WeightedTokensQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.weightedTokensQuery(fn) })
    }

    // percolate helper
    fun percolateQuery(fn: PercolateQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.percolateQuery(fn) })
    }

    // knn helper
    fun knnQuery(fn: KnnQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.knnQuery(fn) })
    }

    // rank_feature helper
    fun rankFeatureQuery(fn: RankFeatureQueryDsl.() -> Unit) {
        addQuery(queryOrNull { this.rankFeatureQuery(fn) })
    }
}
