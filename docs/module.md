# 예약 시스템 모듈 구조

## 1. 개요
예약 서비스는 다수의 API와 배치 워크로드를 분리해 운영하기 위해 Spring Boot 기반 멀티 모듈 구조를 채택했다. 각 모듈은 단일 책임 원칙을 따르며 독립적으로 배포·확장할 수 있다.

```
reservation-server/
├── reservation-common        # 공통 도메인/유틸
├── reservation-auth          # 인증·보안 구성
├── reservation-api           # 사용자(고객) API
├── reservation-admin-api     # 관리자 API
├── reservation-supplier-api  # 공급자 API
└── reservation-batch         # 배치/스케줄 작업
```

## 2. 모듈 역할

### reservation-common
- **목적**: 엔티티, 공통 예외, Validator, QueryDSL 지원 등 모든 모듈이 공유하는 순수 라이브러리
- **빌드**: `java-library (plain jar)`
- **특징**: 웹 의존성을 갖지 않으며 다른 모든 모듈의 기반이 된다.

### reservation-auth
- **목적**: JWT 기반 인증, SecurityConfig, 필터, 인증 헬퍼 제공
- **의존성**: `reservation-common`, Spring Web/Security, JJWT
- **사용처**: 웹 계층이 필요한 모든 API 모듈

### reservation-api / admin-api / supplier-api
- **공통 빌드**: `bootJar`
- **기본 포트**: 8080 / 8081 / 8082
- **주요 기능**
    - reservation-api: 고객 예약 생성/조회, 인증, 결제 연동
    - reservation-admin-api: 사용자 및 공급자 관리, 통계, 정책 설정
    - reservation-supplier-api: 숙소/객실/재고 관리, 정산, 공급자 리포트

### reservation-batch
- **역할**: 스케줄 기반 데이터 정리, 리포트 생성, 알림 발송, 백업 작업
- **실행**: `bootJar` + 스케줄러/CI 연동 또는 수동 실행

## 3. 기술 스택
- **언어**: Java 21
- **프레임워크**: Spring Boot 3.5.6, Spring Security, JPA/QueryDSL
- **빌드 도구**: Gradle 8.x
- **데이터 스토어**: MySQL 8.4, Redis 8.2
- **테스트**: JUnit 5 (`useJUnitPlatform`)
