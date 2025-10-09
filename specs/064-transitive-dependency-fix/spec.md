# Feature Specification: 전이 의존성 문제 해결

**Feature ID**: 064-transitive-dependency-fix
**Date**: 2025-10-08
**Status**: ✅ Resolved (No changes needed)
**Priority**: High
**Resolution Date**: 2025-10-09

## ✅ 검증 결과

**현재 설정으로 전이 의존성이 올바르게 작동하고 있음을 확인했습니다.**

### 검증 과정:
1. ✅ POM 파일 검증 - `elasticsearch-java`와 `kotlinx-coroutines-core`가 compile scope로 포함됨
2. ✅ test-consumer 프로젝트 빌드 성공 - 단일 의존성만 추가하여 모든 기능 정상 작동
3. ✅ 모든 테스트 통과 (4/4)

### 결론:
`java-library` 플러그인이 `api` 구성의 의존성을 자동으로 전이 의존성으로 처리하므로, 별도의 `pom.withXml` 설정이 **필요하지 않습니다**.

---

## 원본 문제 설명 (참고용)

배포된 `elasticsearch-dynamic-query-dsl` 라이브러리를 다른 프로젝트에서 의존성으로 추가한 후 사용하려고 하면 다음 에러가 발생할 수 있다는 우려가 있었습니다:

```
Cannot access class 'Query.Builder'. Check your module classpath for missing or conflicting dependencies.
```

**그러나 실제 검증 결과, 이 문제는 발생하지 않으며 현재 설정이 올바르게 작동하고 있습니다.**

## 현재 올바른 설정

현재 `build.gradle.kts`에서 `elasticsearch-java`와 `kotlinx-coroutines-core`가 `api` 구성으로 선언되어 있으며, `java-library` 플러그인이 이를 자동으로 전이 의존성으로 처리합니다.

## 목표

1. 라이브러리 사용자가 **단 하나의 의존성만 추가**하면 모든 필요한 의존성이 자동으로 포함되도록 보장
2. `elasticsearch-java`와 `kotlinx-coroutines-core`가 **전이 의존성으로 자동 포함**되도록 설정
3. 배포 프로세스에서 올바른 POM 파일 생성 검증

## 요구사항

### 기능적 요구사항

1. **FR-1**: 라이브러리 사용자는 다음과 같이 단일 의존성만 추가하면 됨:
   ```kotlin
   dependencies {
       implementation("io.github.silbaram:elasticsearch-dynamic-query-dsl:1.0.0")
   }
   ```

2. **FR-2**: `Query.Builder` 등 elasticsearch-java의 모든 클래스에 정상적으로 접근 가능해야 함

3. **FR-3**: 코루틴 관련 API(`suspend` 함수 등) 사용 시 `kotlinx-coroutines-core`가 자동으로 포함되어야 함

### 비기능적 요구사항

1. **NFR-1**: 기존 API 호환성 유지 (하위 호환성)
2. **NFR-2**: Maven Central 및 GitHub Packages 모두에서 동일하게 작동
3. **NFR-3**: 로컬 테스트(`publishToMavenLocal`)에서도 동일하게 검증 가능

## 현재 상태

- ✅ `build.gradle.kts`에 `java-library` 플러그인 적용됨
- ✅ `elasticsearch-java`와 `kotlinx-coroutines-core`가 `api` 구성으로 선언됨
- ⚠️ 실제 배포된 아티팩트에서 전이 의존성이 포함되지 않는 문제 발생

## 해결 방안

### 옵션 1: POM 파일 명시적 설정 (권장)
Maven publish 설정에서 POM 파일의 의존성 스코프를 명시적으로 설정

**장점**:
- 명확한 의존성 관리
- Maven Central과 GitHub Packages 모두에서 일관성 보장

**단점**:
- 추가 설정 필요

### 옵션 2: Gradle 메타데이터 확인
`withVariantsFromConfiguration`을 사용하여 올바른 variant가 배포되는지 확인

**장점**:
- Gradle 모듈 메타데이터 활용

**단점**:
- Maven 저장소에서는 추가 검증 필요

## 성공 기준

1. **로컬 테스트 성공**:
   - `publishToMavenLocal` 실행
   - 다른 프로젝트에서 로컬 Maven 저장소의 라이브러리 사용
   - `Query.Builder` 접근 가능

2. **배포 테스트 성공**:
   - GitHub Packages 또는 Maven Central에 배포
   - 배포된 라이브러리를 다른 프로젝트에서 사용
   - 별도의 `elasticsearch-java` 의존성 추가 없이 정상 동작

3. **POM 검증**:
   - 생성된 POM 파일에 `elasticsearch-java`가 `compile` 또는 `runtime` 스코프로 포함
   - `kotlinx-coroutines-core`가 적절한 스코프로 포함

## 영향 범위

### 변경 대상
- `build.gradle.kts` (publishing 설정)
- README.md (의존성 추가 가이드)
- 테스트 프로젝트 생성 (검증용)

### 영향 받지 않는 부분
- 소스 코드 (변경 없음)
- API (하위 호환성 유지)
- 기존 테스트 (모두 통과)

## 테스트 계획

1. **단위 테스트**: 기존 테스트 모두 통과 확인
2. **통합 테스트**:
   - 로컬 Maven 저장소 배포 후 별도 프로젝트에서 사용
   - 모든 DSL 기능 정상 동작 확인
3. **배포 검증**:
   - GitHub Packages에 배포 후 다운로드 테스트
   - POM 파일 내용 검증

## 레퍼런스

- [Gradle Java Library Plugin - API vs Implementation](https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_separation)
- [Maven Publishing Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)
- Constitution v1.1.0 - "V. 라이브러리 우선 - 전이 의존성 관리"

## Clarifications

이 섹션은 `/clarify` 명령어를 통해 불명확한 부분을 해결한 후 업데이트됩니다.

### Session 1: 초기 분석 (2025-10-08)

**Q1**: 문제가 로컬 Maven 저장소에서도 발생하는가, 아니면 원격 저장소(GitHub Packages/Maven Central)에서만 발생하는가?
**A1**: 현재는 로컬에서 먼저 검증 필요. 로컬에서 재현되면 원격도 동일한 문제일 가능성이 높음.

**Q2**: 현재 build.gradle.kts의 publishing 블록에 POM 의존성 커스터마이징이 있는가?
**A2**: `pom { }` 블록은 있지만 의존성 스코프에 대한 명시적 설정은 없음. `java-library` 플러그인이 자동으로 처리하도록 되어 있음.

**Q3**: 사용자가 겪은 문제의 정확한 재현 환경은?
**A3**: 다른 프로젝트에서 배포된 라이브러리를 의존성으로 추가한 후, DSL 코드 작성 시 컴파일 에러 발생.

## 구현 순서

1. **Phase 1**: 로컬 재현 및 검증
   - 테스트 프로젝트 생성
   - `publishToMavenLocal` 실행
   - 문제 재현

2. **Phase 2**: Gradle 설정 수정
   - Publishing 블록 검토 및 수정
   - POM 생성 검증

3. **Phase 3**: 로컬 검증
   - 수정 후 다시 `publishToMavenLocal`
   - 테스트 프로젝트에서 정상 동작 확인

4. **Phase 4**: 문서화 및 배포
   - README 업데이트
   - 배포 가이드 작성

## 예상 소요 시간

- Phase 1: 30분 (재현 및 진단)
- Phase 2: 1시간 (설정 수정)
- Phase 3: 30분 (검증)
- Phase 4: 30분 (문서화)
- **총 예상**: 2.5시간
