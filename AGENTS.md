# Repository Guidelines

## 프로젝트 구조 및 모듈 구성

- `settings.gradle`에 정의된 멀티 모듈 Gradle 프로젝트로, API 경계에 따라 `reservation-api`, `reservation-admin-api`,
  `reservation-supplier-api`, `reservation-auth`, `reservation-batch`가 분리되어 있습니다. 공용 DTO·유틸·예외는 `reservation-common`에
  위치합니다.
- 각 모듈의 애플리케이션 리소스는 `src/main/resources`에, 마이그레이션 스크립트와 레퍼런스 쿼리는 `database/`, 계약 및 시나리오 문서는 `docs/`와 `http/`에 저장합니다.
- 부하·경합 재현 스크립트(`test/concurrent-term-version-test.sh`)는 운영에 준하는 환경을 대상으로 하므로, API 스펙 변경 시 함께 갱신합니다.

## 빌드 · 테스트 · 로컬 실행

- `./gradlew clean build` : Java 21 기준으로 전 모듈을 컴파일하고 JUnit 5 테스트를 수행해 모듈별 JAR을 생성합니다(bootJar 비활성화).
- `./gradlew :reservation-api:bootRun --args='--spring.profiles.active=local'` : 공용 API를 로컬 스택에 연결해 기동합니다. 다른 모듈도 동일한
  패턴으로 실행합니다.
- `./gradlew :reservation-common:test --tests "*TermServiceTest"` : 특정 모듈/클래스만 빠르게 검증할 때 사용합니다.
- `docker compose -f docker/docker-compose.yml up -d mysql redis` : 서비스가 기대하는 MySQL 8.4·Redis 8.2 인프라를 띄웁니다.

## 코딩 스타일 및 네이밍 규칙

- 들여쓰기는 4 spaces, K&R 브레이스 스타일을 유지하고 메서드는 40라인 이내를 권장합니다. Lombok(`@Getter`, `@Builder`)을 기본으로 활용해 보일러플레이트를 줄입니다.
- 패키지는 `com.f1v3.reservation.<모듈>.<도메인>`을 따르며, DTO/엔티티 명은 역할이 드러나도록 `TermCommand`, `ReservationJpaEntity`처럼 기능+타입 조합을
  사용합니다.
- IDE 자동 정렬 또는 `spotless` 도입 시 `./gradlew spotlessApply`로 정렬을 맞춥니다. import 순서는 `java` → `jakarta/spring` → `project`를
  기본으로 합니다.

## 테스트 가이드

- 단위·슬라이스 테스트는 `src/test/java`에서 프로덕션 패키지 구조를 그대로 따릅니다. Spring 기반 테스트는 목적에 맞춰 `@SpringBootTest`, `@DataJpaTest` 등 최소 범위
  애노테이션을 사용합니다.
- 핵심 도메인(약관 버전, 예약 라이프사이클, 인증 흐름)의 회귀 테스트를 필수로 추가하고, 케이스 이름은 `TermService_createTerm_returnsVersionedSnapshot`처럼 시나리오를
  설명합니다.
- 푸시 전 `./gradlew test`를 실행하고, 동시성/계약 검증이 필요한 경우 `bash test/concurrent-term-version-test.sh`를 꼭 돌려 API 변경 위험을 줄입니다.

## 커밋 및 PR 가이드

- Git 히스토리와 동일하게 Conventional 스타일 접두사(`feat:`, `fix:`, `docs:`, `refactor:` 등)를 사용하고, 제목은 72자 이하·영문 요약을 선호합니다(본문에 한국어 설명
  허용).
- PR에는 변경 범위 요약, 테스트 결과(`./gradlew test` 로그 혹은 스크린샷), 연관 이슈 링크, 배포/롤백 플랜을 포함합니다. API 스펙이 바뀌면 `http/`의 예시 요청·응답을 첨부해 리뷰어와
  QA가 즉시 검증할 수 있도록 합니다.