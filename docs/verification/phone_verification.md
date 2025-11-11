# 휴대폰 인증(Phone Verification)

## 1. 목적
회원 가입 및 민감 정보 변경 전에 휴대폰 번호가 실제 사용자 소유인지 검증해 스팸 계정과 중복 가입을 차단한다. 현재는 SMS 전송 대신 서버 로그에 인증번호를 남기지만, 인터페이스는 향후 문자 발송 서비스로 교체할 수 있도록 설계한다.

## 2. 흐름
1. 사용자가 번호를 입력하면 `/v1/verification/phone/request`를 호출한다.
2. 서버는 동일 번호에 대한 최근 요청을 확인하고, 3분 내 재요청이면 기존 코드를 재사용하거나 Ratelimit을 적용한다.
3. 인증번호(5자리)를 생성하여 Redis 혹은 DB에 저장하고, 문자 발송(또는 임시 로그)을 수행한다.
4. 사용자는 받은 코드를 `/v1/verification/phone/confirm`으로 제출한다.
5. 서버는 아래 조건을 모두 만족하면 성공 토큰(`phoneVerificationToken`)을 발급한다.
    - 번호/코드 일치 여부
    - 만료시간(3분) 내인지
    - 시도 횟수(최대 3회) 초과 여부
    - 이미 가입된 사용자 여부 (가입된 경우 실패 처리 혹은 로그인 유도)

## 3. 데이터 모델
```sql
CREATE TABLE phone_verifications (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone_number     VARCHAR(20) NOT NULL,
    verification_code VARCHAR(5) NOT NULL,
    attempt_count    INT         NOT NULL DEFAULT 0,
    is_verified      BOOLEAN     NOT NULL DEFAULT FALSE,
    expired_at       TIMESTAMP   NOT NULL,
    verified_at      TIMESTAMP   NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
- 운영 환경에서는 Redis TTL 키로 대체할 수 있으나, 감사 목적상 RDB에도 로그를 남긴다.

## 4. API 규칙
|Endpoint|설명|
|---|---|
|`POST /v1/verification/phone/request`|전화번호 입력 시 인증번호 발송, 1분 쿨다운|
|`POST /v1/verification/phone/confirm`|코드 검증 후 토큰 발급|

*요청 예시*
```http
POST /v1/verification/phone/confirm

{
  "phoneNumber": "01012345678",
  "code": "84219"
}
```
```json
{
  "token": "verif_e0c41a5f-0c66-4b0e-9d94-b6d8dd71b44d",
  "expiresIn": 600
}
```
- 이 토큰은 회원 가입 API에서 필수 파라미터로 사용되며, 서버 측에서 토큰-전화번호 매핑을 검증한다.

## 5. 보안 고려사항
- 인증번호는 5분 이상 저장하지 않고, 성공 시 즉시 무효화한다.
- 실패 횟수가 3회를 넘으면 일정 시간(예: 10분) 동안 추가 요청을 차단한다.
- 관리자는 최근 인증 시도 로그를 조회해 수상한 패턴(동일 IP 반복 시도 등)을 탐지할 수 있다.
