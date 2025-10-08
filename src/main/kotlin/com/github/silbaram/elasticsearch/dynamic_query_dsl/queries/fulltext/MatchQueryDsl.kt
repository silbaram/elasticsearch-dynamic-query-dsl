package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.ZeroTermsQuery
import co.elastic.clients.util.ObjectBuilder

/**
 * Elasticsearch Match 쿼리를 생성하기 위한 Kotlin DSL 클래스
 *
 * Match 쿼리는 full-text 검색을 위한 가장 기본적인 쿼리 타입입니다.
 * 검색 텍스트를 분석기(analyzer)를 통해 토큰으로 변환한 후,
 * 해당 토큰들이 필드에 포함되어 있는지 검색합니다.
 *
 * **필수 파라미터:**
 * - [field]: 검색 대상 필드명 (null/빈 값이면 쿼리 생성 안 됨)
 * - [query]: 검색 텍스트 (null/빈 값이면 쿼리 생성 안 됨)
 *
 * **기본 옵션:**
 * - [analyzer]: 쿼리 텍스트 분석기
 * - [operator]: 토큰 결합 방식 (AND/OR, 기본값: OR)
 * - [minimumShouldMatch]: 최소 일치 절 수
 *
 * **퍼지 매칭 옵션:**
 * - [fuzziness]: 오타 허용도 ("AUTO", "0", "1", "2")
 * - [prefixLength]: 퍼지 매칭 시 고정 접두사 길이
 * - [maxExpansions]: 퍼지 매칭 시 최대 확장 수
 * - [fuzzyTranspositions]: 인접 문자 전환 허용
 * - [fuzzyRewrite]: 퍼지 쿼리 재작성 방법
 *
 * **고급 옵션:**
 * - [autoGenerateSynonymsPhraseQuery]: 동의어 phrase 쿼리 자동 생성
 * - [lenient]: 타입 불일치 예외 무시
 * - [zeroTermsQuery]: 분석기가 모든 토큰 제거 시 동작 (NONE/ALL)
 *
 * **공통 옵션:**
 * - [boost]: 관련성 점수 가중치
 * - [_name]: 쿼리 이름 (디버깅용)
 *
 * @see [Elasticsearch Match Query 공식 문서](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-match-query.html)
 */
class MatchQueryDsl {
    // 필수 파라미터
    var field: String? = null
    var query: String? = null

    // 기본 옵션
    var analyzer: String? = null
    var operator: Operator? = null
    var minimumShouldMatch: String? = null

    // 퍼지 매칭 옵션
    var fuzziness: String? = null
    var prefixLength: Int? = null
    var maxExpansions: Int? = null
    var fuzzyTranspositions: Boolean? = null
    var fuzzyRewrite: String? = null

    // 고급 옵션
    var autoGenerateSynonymsPhraseQuery: Boolean? = null
    var lenient: Boolean? = null
    var zeroTermsQuery: ZeroTermsQuery? = null

    // 공통 옵션
    var boost: Float? = null
    var _name: String? = null
}

fun Query.Builder.matchQuery(fn: MatchQueryDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = MatchQueryDsl().apply(fn)
    val f = dsl.field?.takeIf { it.isNotBlank() } ?: return this
    val q = dsl.query?.takeIf { it.isNotBlank() } ?: return this

    return this.match { m ->
        m.field(f)
        m.query(q)
        dsl.analyzer?.let { m.analyzer(it) }
        dsl.operator?.let { m.operator(it) }
        dsl.minimumShouldMatch?.let { m.minimumShouldMatch(it) }
        dsl.fuzziness?.let { m.fuzziness(it) }
        dsl.prefixLength?.let { m.prefixLength(it) }
        dsl.maxExpansions?.let { m.maxExpansions(it) }
        dsl.fuzzyTranspositions?.let { m.fuzzyTranspositions(it) }
        dsl.fuzzyRewrite?.let { m.fuzzyRewrite(it) }
        dsl.autoGenerateSynonymsPhraseQuery?.let { m.autoGenerateSynonymsPhraseQuery(it) }
        dsl.lenient?.let { m.lenient(it) }
        dsl.zeroTermsQuery?.let { m.zeroTermsQuery(it) }
        dsl.boost?.let { m.boost(it) }
        dsl._name?.let { m.queryName(it) }
        m
    }
}

