# Architecture Decision Records

`docs/` 폴더에 흩어져 있던 의사결정을 한데 모아 현재 설계 의도를 빠르게 파악할 수 있도록 정리했다. 결정이 바뀌면 해당 항목을 갱신하거나, 새로운 접근 방식을 도입할 때는 ADR을 추가한다.

## ADR-001: 멀티 모듈 Spring Boot 구성
- **Status**: Accepted
- **Context**: 이용자·관리자·공급자용 API와 배치 작업이 분리되어 있으며, 공통 엔티티·예외·유틸리티를 웹 의존성 없이 재사용해야 한다.
- **Decision**: `reservation-common`을 순수 도메인 라이브러리로 두고, `reservation-api`, `reservation-admin-api`, `reservation-supplier-api`는 각각 `bootJar` 실행 모듈로, `reservation-batch`는 스케줄 작업 전용으로 유지한다([docs/module.md](../module.md)).
- **Consequences**: + 모듈별 독립 배포/확장 가능; + 도메인과 웹 계층 분리 유지; − Gradle 설정과 의존성 관리 복잡도가 증가.

## ADR-002: JWT 기반 인증 채택
- **Status**: Accepted
- **Context**: 중앙 세션 저장소 없이 브라우저·네이티브 앱·다중 API를 지원해야 한다.
- **Decision**: 쿠키/세션 대신 리프레시 전략을 포함한 무상태 JWT 액세스 토큰을 사용하고, 클라이언트가 Authorization 헤더로 전달하도록 한다([docs/login/login.md](../login/login.md)).
- **Consequences**: + 수평 확장과 서버 메모리 절감; + 플랫폼 간 일관된 흐름; − 토큰 폐기가 어렵고 클라이언트 저장 정책을 엄격히 관리해야 함.

## ADR-003: 전용 `reservation-auth` 모듈
- **Status**: Accepted
- **Context**: Security 필터는 Spring Web/Security 의존성이 필요하지만 `reservation-common`은 배치 모듈을 위해 web-free로 남아야 한다.
- **Decision**: `reservation-auth` 모듈을 신설해 `reservation-common`을 의존하고 JWT 설정, 필터, Provider를 API 모듈에 제공한다([docs/login/security-module.md](../login/security-module.md)).
- **Consequences**: + 도메인 모듈 경량화; + 인증 로직 재사용성 확보; − 모듈 하나가 추가되어 버전 관리 비용이 늘어남.

## ADR-004: 숙소 상태 흐름과 객실 모델링
- **Status**: Accepted
- **Context**: 공급자가 등록한 숙소는 관리자 승인 후에만 노출되고, 객실 재고는 세밀하게 제어되어야 한다.
- **Decision**: 숙소 상태를 `NONE → PENDING → APPROVED/REJECTED/SUSPENDED` 히스토리와 노출 플래그로 관리하고([docs/accommodation/accommodation.md](../accommodation/accommodation.md)), 객실은 사용자 노출용 `RoomType`과 재고/운영 상태를 가진 `RoomUnit`으로 분리한다([docs/room/room.md](../room/room.md)).
- **Consequences**: + 검수·공급자 행위에 대한 감사 추적 가능; + 유닛 단위 상태로 오버부킹 방지; − 승인 흐름과 검색 필터의 연계가 필요하고 테이블 수가 증가.

## ADR-005: 버전 관리되는 약관 및 동의 저장
- **Status**: Accepted
- **Context**: 약관은 수시로 변경되며, 가입 시점에 어떤 버전에 동의했는지 보존해야 한다.
- **Decision**: `terms` 테이블로 코드/정렬/상태를 관리하고, `term_versions`에 효력 기간을 포함한 본문을 저장하며, `user_term_agreements`에 사용자별 약관/버전 동의 이력을 적재한다([docs/terms/terms.md](../terms/terms.md), [docs/user/user.md](../user/user.md)).
- **Consequences**: + 동의 내역 감사와 약관 선적용 예약 가능; + 관리자 정의 순서를 유연하게 표현; − 가입 시 최신 버전을 해석하기 위한 추가 조인 필요.

## ADR-006: 가입 전 휴대폰 인증 의무화
- **Status**: Accepted
- **Context**: 중복/허위 계정을 방지하고 유효한 연락처만 회원으로 받아야 한다.
- **Decision**: SMS(초기에는 로그 기반) 인증을 선행하고, 3분 유효기간·3회 시도 제한·번호당 1회 검증 규칙을 `phone_verifications` 테이블로 enforced 한다([docs/verification/phone_verification.md](../verification/phone_verification.md)).
- **Consequences**: + 데이터 정합성과 스팸 방지 강화; + 향후 SMS 연동 확장성; − 가입 UX가 한 단계 늘고 단기 저장 데이터가 필요.
