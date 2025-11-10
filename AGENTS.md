# Repository Guidelines

## Project Structure & Module Organization
이 Gradle 멀티 모듈 워크스페이스는 `reservation-*` 네임스페이스의 Spring Boot 서비스들을 중심으로 구성된다. `reservation-api`, `reservation-admin-api`, `reservation-supplier-api`는 `reservation-common`의 공유 코드와 `reservation-auth`의 인증 유틸리티를 사용하며, 정기 작업은 `reservation-batch`에서 처리한다. 모든 모듈은 `src/main/java`와 `src/test/java` 구조를 따르고, 도메인 문서는 `docs/`, SQL·시드 스크립트는 `database/`, 컨테이너 설정은 `docker/`에 정리되어 있다. 모듈 추가·분할 시에는 `docs/module.md`를 기준 문서로 삼는다.

## Build, Test, and Development Commands
- `./gradlew clean build` — 전 모듈을 컴파일하고 단위 테스트를 실행해 산출물을 생성.
- `./gradlew test` — 패키징 없이 빠르게 검증하며 `-p reservation-api` 형태로 범위를 좁힐 수 있음.
- `./gradlew :reservation-api:bootRun` (필요 시 다른 모듈명으로 변경) — 단일 Spring Boot 서비스를 로컬에서 실행.
- `docker compose -f docker/docker-compose.yml up -d` — 개발용 MySQL 8.4와 Redis 8.2를 포트/시드 설정과 함께 기동.

## Coding Style & Naming Conventions
Java 21, 4칸 공백 들여쓰기, `UTF-8` 인코딩을 기본으로 하고 패키지는 `com.f1v3.reservation.<feature>` 형태를 사용한다. DTO·엔티티에는 `@Builder`, `@RequiredArgsConstructor` 등 Lombok을 적극 활용하되, 거래 로직은 각 `reservation-*` 모듈에 두고 공유 엔티티/열거형은 `reservation-common`에 둔다. REST 컨트롤러의 엔드포인트는 케밥 케이스, DTO 필드는 카멜 케이스, 설정 클래스는 모듈별 `config` 패키지에 배치하며 커밋 전 IDE 포맷터로 정렬한다.

## Testing Guidelines
`useJUnitPlatform()`으로 JUnit 5가 전역 적용되어 있다. 테스트 클래스는 해당 모듈의 `*Test`, `*IT` 네이밍을 따르고, 메서드는 `메서드명_should행동` 패턴으로 가독성을 높인다. Mocking으로 충분할 때는 `@WebMvcTest`, `@DataJpaTest` 같은 슬라이스 테스트를 우선 적용하고, 다중 모듈 조합이 필요할 때만 `@SpringBootTest`로 확장한다. 예약 흐름, 가격 계산, 보안 필터 등 핵심 도메인에 회귀 테스트를 추가하고 모든 변경 전 `./gradlew test`를 실행한다.

## Commit & Pull Request Guidelines
Git 히스토리는 `feat:`, `fix:` 등 Conventional Commit 스타일을 유지하며 필요 시 `feat/#14-room`처럼 이슈를 함께 명시한다. 커밋 하나에는 논리적으로 단일 변경만 담고, 이해에 필요한 마이그레이션이나 HTTP 예시는 동일 커밋에 포함한다. PR은 동기, 변경된 엔드포인트, 스키마 영향, 테스트 결과를 명확히 서술하고 API 변경 시 스크린샷이나 cURL 예시를 첨부한다. 브랜치는 항상 `main`에 리베이스하고 복수 모듈에 영향이 있는 경우 모듈 오너에게 조기 리뷰를 요청한다.

## Environment & Configuration Tips
DB 비밀번호, JWT 키 등 비밀 값은 VCS에 포함하지 말고 환경 변수나 Git 무시 대상인 `application-local.yml`에 주입한다. 로컬 실행 시 Spring 프로필을 Docker MySQL(`jdbc:mysql://localhost:3306/reservation`), Redis(`localhost:6379`)에 맞춰 설정하고, 배포나 브랜치 공유 전 `./gradlew clean build`로 CI와 동일한 산출물을 재확인한다.
