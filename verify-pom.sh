#!/bin/bash

# POM 검증 스크립트 - 전이 의존성이 올바르게 설정되었는지 확인

set -e  # 에러 발생 시 스크립트 중단

POM_FILE=~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/1.0.0-SNAPSHOT/elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom

echo "=========================================="
echo "POM 파일 검증 시작"
echo "=========================================="

# 1. POM 파일 존재 확인
if [ ! -f "$POM_FILE" ]; then
    echo "❌ 실패: POM 파일이 존재하지 않습니다: $POM_FILE"
    exit 1
fi
echo "✅ POM 파일 존재 확인: $POM_FILE"

# 2. elasticsearch-java 의존성 확인
if ! grep -q "co.elastic.clients" "$POM_FILE" || ! grep -q "elasticsearch-java" "$POM_FILE"; then
    echo "❌ 실패: elasticsearch-java 의존성이 POM에 없습니다"
    echo "POM 파일 내용:"
    cat "$POM_FILE"
    exit 1
fi
echo "✅ elasticsearch-java 의존성 발견"

# 3. elasticsearch-java의 scope 확인 (compile 또는 비어있어야 함)
ELASTICSEARCH_SCOPE=$(xmllint --xpath "string(//dependency[artifactId='elasticsearch-java']/scope)" "$POM_FILE" 2>/dev/null || echo "")
if [ -z "$ELASTICSEARCH_SCOPE" ] || [ "$ELASTICSEARCH_SCOPE" = "compile" ]; then
    echo "✅ elasticsearch-java scope: ${ELASTICSEARCH_SCOPE:-compile (default)}"
else
    echo "❌ 실패: elasticsearch-java의 scope가 잘못되었습니다: $ELASTICSEARCH_SCOPE (예상: compile 또는 비어있음)"
    exit 1
fi

# 4. kotlinx-coroutines-core 의존성 확인
if ! grep -q "org.jetbrains.kotlinx" "$POM_FILE" || ! grep -q "kotlinx-coroutines-core" "$POM_FILE"; then
    echo "❌ 실패: kotlinx-coroutines-core 의존성이 POM에 없습니다"
    echo "POM 파일 내용:"
    cat "$POM_FILE"
    exit 1
fi
echo "✅ kotlinx-coroutines-core 의존성 발견"

# 5. kotlinx-coroutines-core의 scope 확인
COROUTINES_SCOPE=$(xmllint --xpath "string(//dependency[artifactId='kotlinx-coroutines-core']/scope)" "$POM_FILE" 2>/dev/null || echo "")
if [ -z "$COROUTINES_SCOPE" ] || [ "$COROUTINES_SCOPE" = "compile" ]; then
    echo "✅ kotlinx-coroutines-core scope: ${COROUTINES_SCOPE:-compile (default)}"
else
    echo "❌ 실패: kotlinx-coroutines-core의 scope가 잘못되었습니다: $COROUTINES_SCOPE (예상: compile 또는 비어있음)"
    exit 1
fi

echo "=========================================="
echo "✅ 모든 POM 검증 통과!"
echo "=========================================="
