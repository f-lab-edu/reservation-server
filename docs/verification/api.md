# 휴대폰 인증 API

## 1. 엔드포인트 요약
|Method / Path|설명|
|---|---|
|`POST /v1/verification/phone/request`|인증번호 발송 요청|
|`POST /v1/verification/phone/confirm`|인증번호 확인, 토큰 발급|

## 2. 인증번호 발송
```http
POST /v1/verification/phone/request
Content-Type: application/json

{
  "phoneNumber": "01012345678"
}
```
```json
{
  "expiredAt": "2025-09-20T12:34:56Z",
  "cooldownSeconds": 60
}
```
- 동일 번호는 60초 쿨다운, 하루 5회 이상 요청 시 429 응답.
- 내부적으로 난수(5자리) 생성 후 Redis/DB에 `expiredAt = now + 3m`으로 저장하고, 실제 SMS 연동 전까지 서버 로그에만 출력한다.

## 3. 인증번호 검증
```http
POST /v1/verification/phone/confirm

{
  "phoneNumber": "01012345678",
  "verificationCode": "12345"
}
```
성공 시:
```json
{
  "token": "verif_f757d5d5-1ac0-4af1-9376-3f458b1ff302",
  "expiresIn": 600
}
```
실패 시:
```json
{
  "error": {
    "code": "INVALID_CODE" | "EXPIRED_CODE" | "MAX_ATTEMPTS" | "PHONE_ALREADY_REGISTERED",
    "message": "인증번호가 일치하지 않습니다."
  }
}
```

## 4. 서버 검증 로직
1. `phoneNumber`로 최신 인증 레코드를 조회한다.
2. 만료 여부(`expired_at < now`) 확인 후 `EXPIRED_CODE` 응답.
3. `attempt_count`가 3 이상이면 `MAX_ATTEMPTS`.
4. 이미 가입된 사용자인지 확인하고, 존재 시 `PHONE_ALREADY_REGISTERED`.
5. 코드가 일치하면 `is_verified = true`, `verified_at = now`로 업데이트하고 성공 토큰 발급.
6. 성공 토큰은 가입 시 동일 번호인지 검증하는 데 사용하며, 10분 후 만료된다.
