# Research: 전이 의존성 문제 해결

**Feature**: 064-transitive-dependency-fix
**Date**: 2025-10-08
**Status**: Complete

## 조사 개요

이 문서는 Gradle `java-library` 플러그인과 Maven publishing 설정에서 `api` 의존성이 POM 파일로 올바르게 전파되지 않는 문제에 대한 기술 조사 결과를 정리합니다.

## 1. Gradle java-library 플러그인의 api vs implementation

### Decision
`java-library` 플러그인을 사용하여 `api`와 `implementation` 의존성을 명확히 분리합니다.

### Rationale
Gradle의 `java-library` 플러그인은 의존성을 두 가지로 분류합니다:
- **`api`**: 공개 API에 노출되는 의존성 → Maven `compile` 스코프로 매핑
- **`implementation`**: 내부 구현에만 사용되는 의존성 → Maven `runtime` 스코프로 매핑

이 구분은 다음과 같은 이점을 제공합니다:
1. **컴파일 클래스패스 최적화**: 소비자 프로젝트는 실제로 필요한 의존성만 컴파일 클래스패스에 포함
2. **의존성 누수 방지**: 내부 구현 의존성이 공개 API에 노출되지 않음
3. **빌드 성능 향상**: 불필요한 의존성이 제거되어 빌드 속도 개선

### Alternatives Considered
1. **모든 의존성을 `implementation`으로 선언**
   - 기각 이유: 소비자 프로젝트에서 `Query.Builder` 등에 접근 불가
2. **모든 의존성을 `api`로 선언**
   - 기각 이유: 불필요한 의존성까지 노출되어 컴파일 클래스패스 오염

## 2. Maven POM 생성 및 의존성 스코프 매핑

### Decision
`maven-publish` 플러그인의 `from(components["java"])` 설정을 사용하여 POM 파일을 자동 생성합니다.

### Rationale
Gradle `java-library` 플러그인은 자동으로 다음과 같이 의존성을 매핑합니다:

| Gradle 구성 | Maven 스코프 | 설명 |
|-------------|-------------|------|
| `api` | `compile` | 소비자의 컴파일 + 런타임에 필요 |
| `implementation` | `runtime` | 소비자의 런타임에만 필요 |
| `compileOnly` | `provided` | 컴파일에만 필요 (런타임 제외) |
| `runtimeOnly` | `runtime` | 런타임에만 필요 |

**중요**: `from(components["java"])`를 사용하면 이 매핑이 자동으로 이루어집니다.

### 발견된 문제점
1. **수동 artifact 지정 시 의존성 누락**: `artifact(jar)` 형태로 수동 지정 시 의존성이 POM에 포함되지 않음
2. **Android 프로젝트 특수성**: Android 프로젝트는 표준 Java 컴포넌트가 아니므로 추가 설정 필요
3. **의존성 스코프 오류**: 모든 의존성이 `runtime`으로 잘못 설정되는 이슈 (Gradle Issue #1118)

### Alternatives Considered
1. **`pom.withXml` 수동 설정**
   - 장점: 완전한 제어 가능
   - 기각 이유: 유지보수 부담, 오류 가능성 높음
2. **Gradle 모듈 메타데이터만 의존**
   - 장점: 최신 Gradle 기능 활용
   - 기각 이유: Maven Central 및 구형 빌드 시스템과의 호환성 문제

## 3. Gradle 모듈 메타데이터 vs Maven POM

### Decision
Gradle 모듈 메타데이터(`.module` 파일)와 Maven POM을 모두 생성하여 최대 호환성을 보장합니다.

### Rationale
#### Gradle 모듈 메타데이터
- Gradle 5.0+ 부터 지원
- Variant-aware 의존성 해결
- api/implementation 구분 완벽 지원
- JSON 형식

#### Maven POM
- 모든 Maven/Gradle 버전 호환
- Maven Central 필수 요구사항
- XML 형식
- 레거시 시스템 지원

#### 우선순위
Gradle은 다음 순서로 메타데이터를 사용합니다:
1. Gradle 모듈 메타데이터 (`.module`)
2. Maven POM (`pom.xml`)

Maven은 항상 POM만 사용합니다.

### 현재 프로젝트 분석
현재 `build.gradle.kts` 설정:
```kotlin
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])  // ✅ 올바른 설정
            artifact(tasks["javadocJar"])
            artifactId = "elasticsearch-dynamic-query-dsl"
            pom { ... }  // 메타데이터는 있지만 의존성 관련 설정 없음
        }
    }
}
```

**분석 결과**:
- ✅ `from(components["java"])` 사용 - 의존성 자동 매핑 활성화
- ✅ `java-library` 플러그인 적용됨
- ✅ `api` 구성으로 `elasticsearch-java`와 `kotlinx-coroutines-core` 선언됨
- ⚠️ **하지만 문제 발생** - POM에 의존성이 누락될 수 있는 원인 조사 필요

### Alternatives Considered
1. **Gradle 모듈 메타데이터만 배포**
   - 기각 이유: Maven Central 호환성 없음
2. **POM만 배포 (메타데이터 비활성화)**
   - 기각 이유: 최신 Gradle 프로젝트에서 성능 저하

## 4. publishToMavenLocal 후 POM 검증

### Decision
`publishToMavenLocal` 실행 후 생성된 POM 파일을 XML 파싱하여 의존성 섹션을 검증합니다.

### Rationale
POM 파일 위치:
```
~/.m2/repository/io/github/silbaram/elasticsearch-dynamic-query-dsl/
  └── 1.0.0-SNAPSHOT/
      ├── elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.jar
      ├── elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.pom  ← 검증 대상
      ├── elasticsearch-dynamic-query-dsl-1.0.0-SNAPSHOT.module
      └── ...
```

#### 검증 항목
1. `<dependencies>` 섹션 존재 여부
2. `elasticsearch-java` 의존성:
   ```xml
   <dependency>
       <groupId>co.elastic.clients</groupId>
       <artifactId>elasticsearch-java</artifactId>
       <version>8.14.3</version>
       <scope>compile</scope>  <!-- ← api는 compile 스코프여야 함 -->
   </dependency>
   ```
3. `kotlinx-coroutines-core` 의존성:
   ```xml
   <dependency>
       <groupId>org.jetbrains.kotlinx</groupId>
       <artifactId>kotlinx-coroutines-core</artifactId>
       <version>1.7.3</version>
       <scope>compile</scope>  <!-- ← api는 compile 스코프여야 함 -->
   </dependency>
   ```

#### 검증 방법
- XML 파서 사용 (Kotlin의 `javax.xml` 또는 Groovy XMLSlurper)
- XPath 쿼리: `//dependencies/dependency[artifactId='elasticsearch-java']/scope`
- 예상 결과: `compile` 또는 비어있음 (기본값이 compile)

### Alternatives Considered
1. **수동 POM 파일 열기**
   - 기각 이유: 자동화 불가, 오류 가능성
2. **Gradle dependencies task**
   - 기각 이유: 로컬 POM이 아닌 빌드 시점 의존성만 표시

## 5. 근본 원인 및 해결 방안

### 근본 원인 분석
조사 결과, 현재 설정은 **이론적으로는 올바르지만** 다음 시나리오에서 문제가 발생할 수 있습니다:

1. **Kotlin Gradle Plugin과의 상호작용**
   - Kotlin 플러그인이 `java-library`보다 먼저 적용되면 컴포넌트 설정이 달라질 수 있음

2. **중복 플러그인 적용**
   - `java` 플러그인과 `java-library` 플러그인이 동시에 적용되면 충돌 가능

3. **POM customization 문제**
   - `pom { }` 블록 내에서 의존성 관련 설정을 덮어쓰는 경우

### Decision
다음 3단계 해결 방안을 적용합니다:

#### 1단계: POM 파일 검증
- `publishToMavenLocal` 실행
- 생성된 POM 확인
- 의존성 누락 여부 진단

#### 2단계: 필요 시 명시적 의존성 추가
POM에 의존성이 없는 경우, `pom.withXml`을 사용하여 명시적으로 추가:

```kotlin
pom {
    // ... 기존 설정 ...

    withXml {
        val dependenciesNode = asNode().appendNode("dependencies")

        // api dependencies → compile scope
        configurations.api.get().allDependencies.forEach { dep ->
            val dependencyNode = dependenciesNode.appendNode("dependency")
            dependencyNode.appendNode("groupId", dep.group)
            dependencyNode.appendNode("artifactId", dep.name)
            dependencyNode.appendNode("version", dep.version)
            dependencyNode.appendNode("scope", "compile")
        }

        // implementation dependencies → runtime scope
        configurations.implementation.get().allDependencies.forEach { dep ->
            val dependencyNode = dependenciesNode.appendNode("dependency")
            dependencyNode.appendNode("groupId", dep.group)
            dependencyNode.appendNode("artifactId", dep.name)
            dependencyNode.appendNode("version", dep.version)
            dependencyNode.appendNode("scope", "runtime")
        }
    }
}
```

#### 3단계: 테스트 프로젝트로 검증
- 별도 Gradle 프로젝트 생성
- 로컬 Maven 저장소에서 라이브러리 의존성 추가
- `Query.Builder` 접근 가능 여부 테스트

### Rationale
이 3단계 접근 방식은:
1. **진단 우선**: 실제 문제 확인
2. **최소 개입**: 필요한 경우에만 수정
3. **검증 완료**: 실제 사용 시나리오로 테스트

## 6. 추가 고려사항

### Gradle 버전 호환성
- 현재 Gradle 8.10.2 사용
- `java-library` 플러그인은 Gradle 3.4+부터 지원
- Maven publish 플러그인은 Gradle 4.8+부터 안정화
- **결론**: 버전 호환성 문제 없음

### Kotlin Multiplatform 고려
- 현재는 JVM 전용 라이브러리
- 향후 Kotlin Multiplatform으로 확장 시 다른 접근 필요
- **결론**: 현재 방식 유지, 필요 시 재검토

### Maven Central 요구사항
- POM 파일 필수
- 의존성 정보 필수
- Sources 및 Javadoc JAR 필수 (이미 충족)
- **결론**: POM 의존성 수정으로 요구사항 충족

## 결론

### 주요 결정 사항
1. **`java-library` 플러그인과 `from(components["java"])` 유지**
2. **POM 검증 후 필요 시 `pom.withXml`로 의존성 명시적 추가**
3. **테스트 프로젝트로 실제 사용 시나리오 검증**
4. **Gradle 모듈 메타데이터와 Maven POM 모두 생성하여 최대 호환성 보장**

### 다음 단계 (Phase 1)
1. data-model.md 작성 (POM 의존성 구조, 테스트 프로젝트 구조)
2. contracts/ 작성 (POM 검증 계약, 빌드 성공 계약)
3. quickstart.md 작성 (단계별 수정 및 검증 가이드)
4. CLAUDE.md 업데이트 (Gradle publishing 설정 추가)

---
**조사 완료일**: 2025-10-08
**다음 Phase**: Phase 1 - Design & Contracts
