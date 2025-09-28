# 회원 (User)

## 1. 회원 가입 API

```http request
POST /v1/users/sign-up

REQEUST:
{
    "email": "seungjo@gmail.com",
    "password": "p@ssw0rd!",
    "nickname": "tester",
    "phoneNumber": "010-0000-0000", // 인증이 완료된 번호인지 확인 필요
    "birthDate": "1999-05-13",
    "gender": "M" | "F",
    "agreeTerms": [
        {
            "termId": 1,        // 약관 ID
            "termVersion": 1    // 약관 버전
        },
        {
            "termId": 2,
            "termVersion": 3
        }
    ]
}

RESPONSE:
{
    "userId": 12345 // 생성된 사용자 ID
}
```

- 핸드폰 번호를 기반으로 인증이 완료된 회원인지 검증 필요
  - 또한, 인증을 진행한지 10분 이내인지도 확인 필요
- terms 필드는 약관 ID, 약관 버전 ID 리스트로, 사용자가 선택한 약관들을 나타낸다.
  - 필수 약관은 무조건 포함되어야 한다.