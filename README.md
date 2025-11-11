# 숙소 예약 서비스

다중 역할(사용자·공급자·관리자)을 가진 숙소 예약 플랫폼으로, Spring Boot 기반 멀티 모듈 구조를 통해 API 서비스와 배치 작업을 분리해 운영합니다. `reservation-common`에 공통 도메인을
집약하고, 인증은 `reservation-auth` 모듈을 통해 재사용합니다.

## 프로젝트 구조

```
reservation/
├── reservation-common        # 공용 엔티티/예외/유틸
├── reservation-auth          # JWT·Security 구성요소
├── reservation-api           # 고객용 API (예약, 결제, 조회)
├── reservation-admin-api     # 관리자용 API (정책·통계·계정 관리)
├── reservation-supplier-api  # 공급자용 API (숙소/객실/재고 관리)
└── reservation-batch         # 스케줄/정산/리포트 배치
```

| 모듈                       | 타입           | 주요 역할                                    |
|--------------------------|--------------|------------------------------------------|
| reservation-common       | java-library | 엔티티, Validator, 공통 예외, QueryDSL 지원       |
| reservation-auth         | bootJar      | JWT 발급/검증, Spring Security Config, 인증 필터 |
| reservation-api          | bootJar      | 고객 인증, 예약 생성/조회, 결제 연동                   |
| reservation-admin-api    | bootJar      | 사용자·정책 관리, 대시보드/통계                       |
| reservation-supplier-api | bootJar      | 숙소 정보, 객실 재고, 정산/리포트                     |
| reservation-batch        | bootJar      | 데이터 정리, 알림, 주기적 리포트                      |

## 기술 스택

- Java 21, Spring Boot 3.5.x, Spring Security, JPA/QueryDSL
- Gradle 8.x, Lombok, JUnit 5 (`useJUnitPlatform`)
- MySQL 8.4, Redis 8.2 (Docker Compose로 제공)

## 디렉터리 가이드

- `docs/` — 도메인 문서 및 ADR (`docs/module.md`에서 모듈 상세 확인)
- `database/` — DDL, 마이그레이션, 시드 스크립트
- `docker/` — 개발용 MySQL·Redis Compose 설정
- `http/` — cURL/HTTP 예제 모음
- `test/` — 통합 혹은 E2E 스크립트

## 커밋 규칙

- Conventional Commit(`feat:`, `fix:` 등)과 단일 책임 커밋 유지
- 브랜치는 `main` 기준 리베이스, 다중 모듈 영향 시 담당자와 조기 공유
- PR에는 변경 배경, 엔드포인트/스키마 영향, 테스트 결과, 필요 시 스크린샷 또는 cURL 예시를 포함합니다.

