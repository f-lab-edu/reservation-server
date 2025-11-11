# 회원 API 명세

## 1. 엔드포인트 요약
|분류|Method / Path|설명|
|---|---|---|
|공통|`POST /v1/users/sign-up`|휴대폰 인증 이후 회원 가입|
|공통|`POST /v1/users/login`|이메일/비밀번호 기반 로그인 (JWT 발급)|
|회원|`GET /v1/users/me`|내 프로필 조회|
|회원|`PATCH /v1/users/me`|프로필 수정(닉네임, 알림 설정 등)|
|회원|`PATCH /v1/users/me/password`|비밀번호 변경|
|회원|`GET /v1/users/me/agreements`|동의한 약관 목록 조회|
|관리자|`GET /v1/admin/users`|회원 검색/조회 (필터)|

## 2. 회원 가입
```http
POST /v1/users/sign-up
Content-Type: application/json

{
  "email": "seungjo@gmail.com",
  "password": "p@ssw0rd!",
  "nickname": "tester",
  "phoneNumber": "01012345678",
  "birthDate": "1999-05-13",
  "gender": "MALE",
  "agreedTerms": [
    {"termCode": "TERM_SERVICE", "version": 2},
    {"termCode": "TERM_PRIVACY", "version": 3}
  ],
  "phoneVerificationToken": "verif_abc123"   // 휴대폰 인증 성공 토큰
}
```
```json
{
  "userId": 12345
}
```
- 서버는 `phoneVerificationToken`으로 3분 이내 인증 여부를 확인한다.
- `agreedTerms`는 필수 약관이 모두 포함되어야 하며, 유효하지 않은 코드/버전 조합이면 400을 반환한다.
- 비밀번호는 서버에서 BCrypt 해시 후 저장한다.

## 3. 로그인
```http
POST /v1/users/login

{
  "email": "seungjo@gmail.com",
  "password": "p@ssw0rd!"
}
```
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "def50200...",
  "expiresIn": 3600,
  "user": {
    "id": 12345,
    "role": "USER",
    "nickname": "tester"
  }
}
```
- 구현 세부사항은 [docs/login/login.md](../login/login.md) 참고.

## 4. 프로필 조회/수정
```http
GET /v1/users/me
Authorization: Bearer {access_token}
```
```json
{
  "id": 12345,
  "email": "seungjo@gmail.com",
  "nickname": "tester",
  "phoneNumber": "01012345678",
  "birthDate": "1999-05-13",
  "gender": "MALE",
  "role": "USER",
  "notification": {
    "marketing": true,
    "sms": true,
    "email": false
  }
}
```

수정 예시:
```http
PATCH /v1/users/me
Content-Type: application/json

{
  "nickname": "newTester",
  "notification": {
    "marketing": false,
    "email": true
  }
}
```
- 휴대폰 번호 변경 시에는 재인증 토큰을 요구한다.

## 5. 비밀번호 변경
```http
PATCH /v1/users/me/password

{
  "currentPassword": "p@ssw0rd!",
  "newPassword": "NewP@ssw0rd1"
}
```
- 실패 시 `401 INVALID_CREDENTIALS`, 최근 24시간 내 동일 비밀번호 재사용 제한 등 정책을 적용한다.

## 6. 약관 동의 내역
```http
GET /v1/users/me/agreements
```
```json
{
  "agreements": [
    {
      "termCode": "TERM_SERVICE",
      "termVersion": 2,
      "title": "서비스 이용약관",
      "type": "REQUIRED",
      "agreedAt": "2025-01-01T12:00:00Z"
    }
  ]
}
```
- 사용자가 약관을 재동의하면 새 레코드를 추가하고, 최신 동의만 UI에 표기한다.

## 7. 관리자 회원 검색
```http
GET /v1/admin/users?email=seungjo@gmail.com&role=SUPPLIER&status=ACTIVE&page=0&size=20
Authorization: Bearer {admin_access_token}
```
응답에는 기본 프로필, 역할, 최근 로그인, 가입 일시, 제재 상태가 포함된다. 개인정보 보호를 위해 민감 정보는 `ADMIN` 권한에서만 조회 가능하다.
