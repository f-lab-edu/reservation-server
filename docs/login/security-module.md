# Security 의존성은 어디에 있어야 하는가?

### 현재 프로젝트 구조

```
reservation/
├── reservation-common/      // 도메인, 공통 유틸리티 (JPA, Validation, QueryDSL)
├── reservation-api/         // 사용자 API (Web)
├── reservation-admin-api/   // 관리자 API (Web)  
├── reservation-supplier-api/ // 공급업체 API (Web)
└── reservation-batch/       // 배치 작업 (Non-Web)
```

**현재 의존성 상태**:

- `reservation-common`: JPA(Entity, Repository), QueryDSL, Common Response/Exception 등
- `reservation-api`: Web + Common 의존성
- `reservation-admin-api`: Web + Common 의존성
- `reservation-supplier-api`: Web + Common 의존성
- `reservation-batch`: Common 의존성 (Web 없음)

## 핵심 고민사항

Spring Security를 활용한 JWT 인증 시스템을 어디에 위치시킬 것인가?

### 현재 제약 조건

1. **`reservation-common` 모듈에는 `web` 의존성을 넣지 않는 구조 유지**
    - `reservation-batch` 모듈이 `common` 모듈을 참조
    - 배치 작업에서 web 의존성은 불필요하고 아키텍처적으로 부적절
    - 도메인과 웹 계층의 관심사 분리 원칙

2. **Security 필터 구현 시 필요한 의존성**
    - `OncePerRequestFilter` → Spring Web 의존성 필요
    - `HttpServletRequest`, `HttpServletResponse` → Servlet API 필요
    - `SecurityConfig` → Spring Security + Web 설정

3. **JWT 토큰 처리**
    - 토큰 생성/검증 로직 → 순수 라이브러리 (Web 불필요)
    - 토큰 추출 (Header, Cookie) → Web 의존성 필요

## 검토한 방안

### 방안 1. common 모듈에 web 의존성 추가

```gradle
// reservation-common/build.gradle
dependencies {
    // ...existing dependencies...
    api 'org.springframework.boot:spring-boot-starter-web'
    api 'org.springframework.boot:spring-boot-starter-security'
}
```

**장점**:

- 구조가 단순함
- 모든 모듈에서 Security 기능 사용 가능

**단점**:

- **배치 모듈에 불필요한 web 의존성 포함**
- **도메인 계층과 웹 계층의 관심사 혼재**

### 방안 2. 별도 `reservation-auth` 모듈 생성

```
reservation/
├── reservation-common          // 도메인만 (Web 의존성 없음)
├── reservation-auth            // 보안 전용 모듈 (Web + Security)
├── reservation-api             // Security 모듈 의존
├── reservation-admin-api       // Security 모듈 의존
├── reservation-supplier-api    // Security 모듈 의존
└── reservation-batch           // Common만 의존 (Web 없음)
```

**의존성 구조**

```gradle
// reservation-security/build.gradle
dependencies {
    implementation project(':reservation-common')
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    implementation 'io.jsonwebtoken:jjwt-impl:0.11.5'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.11.5'
}

// reservation-api/build.gradle  
dependencies {
    implementation project(':reservation-common')
    implementation project(':reservation-security')
    implementation 'org.springframework.boot:spring-boot-starter-web'
}

// reservation-batch/build.gradle
dependencies {
    implementation project(':reservation-common') // Web 의존성 없음
}
```

**장점**

- **관심사 분리**: 보안 로직이 독립적으로 관리
- **재사용성**: 여러 API 모듈에서 동일한 보안 설정 사용
- **의존성 격리**: 배치는 순수 도메인만 사용
- **확장성**: 향후 보안 요구사항 변경 시 security 모듈만 수정

**단점**

- 모듈 개수 증가로 인한 복잡성
- API 모듈에서 2개 모듈 의존 필요

### 방안 3. `common` 모듈 안에 서브 모듈로 분리

```
reservation-common/
├── reservation-common-domain/    // 순수 도메인
├── reservation-common-security/  // 보안 (Web 의존성 포함)
└── reservation-common-support/   // 공통 유틸리티
```

- 이 방식은 방안 2와 유사한 방식으로 보임.
- 다만, common 모듈 내에 `web` 의존성이 포함된 서브 모듈이 존재하는게 맞는가에 대한 판단이 모호함.

## 실제 구현 시 고려사항

### JWT 토큰 처리의 이중 구조 문제

**문제**: JWT 관련 기능을 어떻게 분리할 것인가?

```java
// 순수 JWT 로직 (Web 불필요) - common에 위치 가능
public class JwtTokenProvider {
    public String generateToken(Authentication auth) { ...}

    public boolean validateToken(String token) { ...}

    public Claims getClaimsFromToken(String token) { ...}
}

// Web 기반 JWT 처리 (Web 필요) - security 모듈에 위치
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public void doFilterInternal(HttpServletRequest request, ...) { ...}
}
```

## 결론 및 권장사항

### 방안 2 채택

별도 `reservation-auth` 모듈 생성하는 방식으로 진행

1. 현재 아키텍처 원칙 유지 (common의 web 의존성 배제)
2. 배치 모듈의 순수성 보장 (web 의존성이 있어야 하는가?)

### 실제 구현 구조

```
// reservation-auth 모듈 구성
com.f1v3.reservation.auth/
├──config/
│   └──SecurityConfig.java
├──filter/
│   └──JwtAuthenticationFilter.java
├──provider/
│   └──JwtTokenProvider.java
└──service/
    └──TokenAuthenticationService.java
```
