package com.github.silbaram.elasticsearch.dynamic_query_dsl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe

class ElasticsearchClientVersionTest: FunSpec({
    test("클라이언트 버전을 조회할 수 있어야 한다") {
        elasticsearchClientVersion() shouldNotBe null
    }
})
