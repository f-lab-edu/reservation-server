# Security 모듈 구조

## 1. 목표
Spring Security + JWT 구성을 재사용 가능한 모듈로 분리하여 API 모듈과 배치 모듈의 관심사를 나눈다. `reservation-common`은 순수 도메인만 포함하고, 웹/보안 의존성은 `reservation-auth`에만 배치한다.

## 2. 의존성 배치
```
reservation-common   # web 의존성 없음
reservation-auth     # Spring Web + Security + JWT
├── config/          # SecurityFilterChain, PasswordEncoder 등
├── filter/          # JwtAuthenticationFilter
├── provider/        # JwtTokenProvider, TokenParser
└── service/         # TokenAuthenticationService, LogoutService

reservation-api, reservation-admin-api, reservation-supplier-api
 └─ implementation project(':reservation-auth')

reservation-batch
 └─ implementation project(':reservation-common')
```

## 3. 주요 구성 요소
|구성|설명|
|---|---|
|`SecurityConfig`|모든 API 모듈에서 import, CORS/CSRF 정책과 권한 매핑 정의|
|`JwtAuthenticationFilter`|`OncePerRequestFilter`를 구현하여 Authorization 헤더에서 토큰 추출|
|`JwtTokenProvider`|서명/검증/클레임 추출 담당, 테스트 가능하도록 순수 로직 유지|
|`AuthenticationEntryPoint`/`AccessDeniedHandler`|API 응답 규격에 맞춘 에러 포맷 반환|
|`TokenBlacklistService`|로그아웃 및 강제 만료를 위해 Redis/DB와 연동|

## 4. 모듈 선택 근거
- `reservation-common`에 Web 의존성을 추가하면 배치 모듈까지 Servlet 스택을 참조하게 되어 아키텍처 원칙이 무너진다.
- 독립 모듈로 분리하면 인증 로직을 일괄적으로 버전 관리할 수 있고, 다수의 API 모듈에서 보안 설정을 공유할 수 있다.
- 향후 OAuth, SSO, WebFlux 등 다른 보안 요구사항이 생겨도 `reservation-auth`만 교체하면 된다.

## 5. 적용 방법
1. API 모듈의 `build.gradle`에 `implementation project(':reservation-auth')` 추가.
2. `Application` 클래스에서 `@Import(SecurityConfig.class)` 혹은 컴포넌트 스캔으로 설정을 가져온다.
3. 각 모듈은 Security DSL에서 자신의 엔드포인트 권한만 정의하면 되고, 공통 필터/예외 처리는 `reservation-auth`에 둔다.
