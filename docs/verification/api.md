# 핸드폰 인증 (Phone Verification)

## 1. 인증번호 발송 API

> 랜덤한 숫자 생성 후 로그 출력하는 방식으로 구현한다.

```http request
POST /v1/sign-up/phone/verification/send-code

REQUEST:
{
    "phoneNumber": "010-0000-0000",
}

RESPONSE:
{
   "expiredAt": "2025-09-20T12:34:56" // 인증번호 만료 시간
}
```

- 3분 이내 동일한 휴대폰 번호로 재요청 불가
- 내부적으로 생성된 인증번호는 3분간 유효

## 2. 인증번호 확인 API

```http request
POST /v1/sign-up/phone/verification/verify-code

REQUEST:
{
    "phoneNumber": "010-0000-0000",
    "verificationCode": "1234"
}

RESPONSE:

[성공 케이스]
{
    "success": true,
    "error": null
}

[실패 케이스]
{
    "success": false,
    "error": {
        "code": "INVALID_CODE" | "EXPIRED_CODE" | "MAX_ATTEMPTS_EXCEEDED" | "PHONE_ALREADY_REGISTERED",
        "message": "인증에 실패한 사유 메시지"
    }
}
```

**검증 절차**

1. DB에서 전화번호와 인증 코드를 읽은 후 인증 코드가 일치하는지 확인
    - 만약 3회 이상 틀렸을 경우 "MAX_ATTEMPTS_EXCEEDED" 예외 발생, 사용불가 처리
2. 인증 코드 생성 시점이 3분 이내인지 확인
3. 이미 가입된 회원인지 확인
4. 모든 검증이 통과하면 인증 성공 처리

