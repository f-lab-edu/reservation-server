# 로그인 설계 및 인증 방식 비교

## 1. 고려한 인증 방식

|방식|장점|한계|
|---|---|---|
|쿠키 기반(브라우저 세션)|구현이 단순하고 브라우저가 자동 관리|XSS/CSRF 취약, 4KB 제한, 모바일/외부 클라이언트 지원 어려움|
|서버 세션 기반|중앙에서 무효화 가능, 민감 정보 서버 보관|상태 유지 필요, 다중 서버에서 세션 스토어/Sticky Session 필수, 메모리 사용량 증가|
|JWT 기반|Stateless, 수평 확장 용이, 웹/앱/외부 파트너가 공통 프로토콜 사용|토큰 폐기 어려움, 클라이언트 저장소 보안 고려, 갱신 로직 추가 필요|

## 2. 최종 결정: JWT + Refresh Token
- 멀티 모듈/멀티 인스턴스 아키텍처에서 세션 동기화를 피하고자 Stateless 전략을 채택했다.
- Access Token은 1시간, Refresh Token은 14일 유효로 설정하며, Redis 블랙리스트를 통해 강제 만료를 지원한다.
- 공급자/관리자 콘솔도 동일 토큰 포맷을 사용해 SSO에 대비한다.

## 3. 인증 흐름
1. 사용자가 `/v1/users/login`으로 이메일/비밀번호를 제출하면 인증 서비스가 사용자 자격 증명을 검증한다.
2. 성공 시 Access/Refresh Token을 발급하고, Refresh Token은 Redis에 `userId`와 함께 저장(혹은 해시)한다.
3. 클라이언트는 Access Token을 `Authorization: Bearer` 헤더로 전송한다.
4. 토큰이 만료되면 `/v1/auth/token/refresh`로 새 Access Token을 요청한다.
5. 로그아웃 또는 강제 만료 시 Refresh Token을 삭제하고, Access Token JTI를 블랙리스트에 추가한다.

## 4. 보안 수칙
- JWT에는 최소한의 정보(`sub`, `role`, `tokenType`, `exp`)만 포함한다.
- 서명 알고리즘은 HS512, 키는 Secrets Manager에서 로테이션한다.
- 2FA(추후 도입)를 위해 로그인 성공 시 OTP 단계가 필요한 계정은 `authContext`에 별도 플래그를 포함한다.

## 5. 실패 처리
- 비밀번호 5회 이상 오류 시 10분간 로그인 제한.
- 휴면 계정/차단 계정은 `ACCOUNT_LOCKED` 에러 코드로 응답하고, 관리 대시보드에서 해제할 수 있다.

## 6. 참고 문서
- Security 모듈 구성: [docs/login/security-module.md](./security-module.md)
- 회원 API: [docs/user/api.md](../user/api.md)
