package com.github.silbaram.elasticsearch.dynamic_query_dsl.queries.fulltext

import co.elastic.clients.elasticsearch._types.query_dsl.Like
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.util.ObjectBuilder

/**
 * more_like_this 쿼리 DSL 빌더
 * - like 텍스트/문서를 기준으로 유사 문서를 검색
 * - like 항목이 비면 생성하지 않음 (생략)
 */
fun Query.Builder.moreLikeThis(
    likeTexts: List<String> = emptyList(),
    likeDocs: List<Pair<String, String>> = emptyList(), // (index, id)
    unlikeTexts: List<String> = emptyList(),
    unlikeDocs: List<Pair<String, String>> = emptyList(),
    fields: List<String> = emptyList(),
    analyzer: String? = null,
    minimumShouldMatch: String? = null,
    minTermFreq: Int? = null,
    maxQueryTerms: Int? = null,
    minDocFreq: Int? = null,
    maxDocFreq: Int? = null,
    minWordLength: Int? = null,
    maxWordLength: Int? = null,
    stopWords: List<String> = emptyList(),
    boost: Float? = null,
    _name: String? = null
): ObjectBuilder<Query> {
    // Build Like/Unlike lists
    val likeItems = mutableListOf<Like>()
    likeItems.addAll(likeTexts.filter { it.isNotBlank() }.map { t -> Like.of { it.text(t) } })
    likeItems.addAll(
        likeDocs
            .filter { it.first.isNotBlank() && it.second.isNotBlank() }
            .map { (index, id) ->
                Like.of { l -> l.document { d -> d.index(index).id(id) } }
            }
    )

    if (likeItems.isEmpty()) return this

    val unlikeItems = mutableListOf<Like>()
    unlikeItems.addAll(unlikeTexts.filter { it.isNotBlank() }.map { t -> Like.of { it.text(t) } })
    unlikeItems.addAll(
        unlikeDocs
            .filter { it.first.isNotBlank() && it.second.isNotBlank() }
            .map { (index, id) ->
                Like.of { l -> l.document { d -> d.index(index).id(id) } }
            }
    )

    return this.moreLikeThis { mlt ->
        likeItems.forEach { mlt.like(it) }
        unlikeItems.forEach { mlt.unlike(it) }
        if (fields.isNotEmpty()) mlt.fields(fields)
        analyzer?.let { mlt.analyzer(it) }
        minimumShouldMatch?.let { mlt.minimumShouldMatch(it) }
        minTermFreq?.let { mlt.minTermFreq(it) }
        maxQueryTerms?.let { mlt.maxQueryTerms(it) }
        minDocFreq?.let { mlt.minDocFreq(it) }
        maxDocFreq?.let { mlt.maxDocFreq(it) }
        minWordLength?.let { mlt.minWordLength(it) }
        maxWordLength?.let { mlt.maxWordLength(it) }
        if (stopWords.isNotEmpty()) mlt.stopWords(stopWords)
        boost?.let { mlt.boost(it) }
        _name?.let { mlt.queryName(it) }
        mlt
    }
}

// --- Builder-style DSL overload (preferred) ---
class MoreLikeThisDsl {
    internal val likeItems = mutableListOf<Like>()
    internal val unlikeItems = mutableListOf<Like>()
    internal val fieldsList = mutableListOf<String>()
    internal val stopWordsList = mutableListOf<String>()

    var analyzer: String? = null
    var minimumShouldMatch: String? = null
    var minTermFreq: Int? = null
    var maxQueryTerms: Int? = null
    var minDocFreq: Int? = null
    var maxDocFreq: Int? = null
    var minWordLength: Int? = null
    var maxWordLength: Int? = null
    var boost: Float? = null
    var _name: String? = null

    fun fields(vararg fields: String) {
        fieldsList.addAll(fields.filter { it.isNotBlank() })
    }

    fun stopWords(vararg words: String) {
        stopWordsList.addAll(words.filter { it.isNotBlank() })
    }

    fun like(fn: LikeCollector.() -> Unit) {
        val c = LikeCollector().apply(fn)
        likeItems.addAll(c.items)
    }

    fun unlike(fn: LikeCollector.() -> Unit) {
        val c = LikeCollector().apply(fn)
        unlikeItems.addAll(c.items)
    }
}

class LikeCollector {
    internal val items = mutableListOf<Like>()

    fun text(vararg texts: String) {
        texts.filter { it.isNotBlank() }
            .map { t -> Like.of { it.text(t) } }
            .let { items.addAll(it) }
    }

    fun doc(index: String, id: String) {
        if (index.isBlank() || id.isBlank()) return
        items.add(Like.of { l -> l.document { d -> d.index(index).id(id) } })
    }
}

fun Query.Builder.mlt(fn: MoreLikeThisDsl.() -> Unit): ObjectBuilder<Query> {
    val dsl = MoreLikeThisDsl().apply(fn)
    if (dsl.likeItems.isEmpty()) return this

    return this.moreLikeThis { mlt ->
        dsl.likeItems.forEach { mlt.like(it) }
        dsl.unlikeItems.forEach { mlt.unlike(it) }
        if (dsl.fieldsList.isNotEmpty()) mlt.fields(dsl.fieldsList)
        dsl.analyzer?.let { mlt.analyzer(it) }
        dsl.minimumShouldMatch?.let { mlt.minimumShouldMatch(it) }
        dsl.minTermFreq?.let { mlt.minTermFreq(it) }
        dsl.maxQueryTerms?.let { mlt.maxQueryTerms(it) }
        dsl.minDocFreq?.let { mlt.minDocFreq(it) }
        dsl.maxDocFreq?.let { mlt.maxDocFreq(it) }
        dsl.minWordLength?.let { mlt.minWordLength(it) }
        dsl.maxWordLength?.let { mlt.maxWordLength(it) }
        if (dsl.stopWordsList.isNotEmpty()) mlt.stopWords(dsl.stopWordsList)
        dsl.boost?.let { mlt.boost(it) }
        dsl._name?.let { mlt.queryName(it) }
        mlt
    }
}
