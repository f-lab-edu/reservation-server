# 약관 API 명세

## 1. 엔드포인트 요약
|분류|Method / Path|설명|
|---|---|---|
|사용자|`GET /v1/sign-up/terms`|가입 화면에 노출할 최신 약관 목록 조회|
|관리자|`GET /v1/admin/terms`|약관 + 버전 전체 목록 (필터/정렬)| 
|관리자|`POST /v1/admin/terms`|새 약관 코드 생성 및 최초 버전 등록|
|관리자|`PUT /v1/admin/terms/{termCode}/versions`|기존 약관 코드에 새 버전 추가|
|관리자|`PATCH /v1/admin/terms/{termCode}`|제목, 타입, 정렬, 상태 변경|

## 2. 사용자 약관 목록
```http
GET /v1/sign-up/terms
```
```json
{
  "terms": [
    {
      "termId": 1,
      "termCode": "TERM_OF_SERVICE",
      "title": "이용약관 (필수)",
      "type": "REQUIRED",
      "version": 2,
      "displayOrder": 1,
      "content": "제1조 목적 ..."
    },
    {
      "termId": 3,
      "termCode": "TERM_OF_MARKETING",
      "title": "마케팅 수신 동의 (선택)",
      "type": "OPTIONAL",
      "version": 1,
      "displayOrder": 3,
      "content": "서비스 소식, 프로모션 ..."
    }
  ]
}
```
- 내부 조건: `status = ACTIVE`, `is_current = true`, `effective_date <= now`, `expiry_date IS NULL OR expiry_date > now`.
- `title` 뒤 `(필수)` `(선택)` 문구는 API에서 직접 붙여 UI 처리 부담을 줄인다.

## 3. 관리자 약관 목록
```http
GET /v1/admin/terms?code=TERM_OF_SERVICE&includeHistory=true
Authorization: Bearer {admin_access_token}
```
```json
[
  {
    "termId": 1,
    "termCode": "TERM_OF_SERVICE",
    "title": "서비스 이용 약관",
    "type": "REQUIRED",
    "displayOrder": 100,
    "status": "ACTIVE",
    "version": 3,
    "isCurrent": true,
    "content": "...",
    "effectiveDateTime": "2025-09-25T00:00:00",
    "expiryDateTime": null,
    "createdAt": "2025-09-20T06:00:00",
    "updatedAt": "2025-09-25T06:00:00"
  },
  {
    "termId": 1,
    "termCode": "TERM_OF_SERVICE",
    "title": "서비스 이용 약관",
    "type": "REQUIRED",
    "displayOrder": 100,
    "status": "ACTIVE",
    "version": 2,
    "isCurrent": false,
    "content": "...",
    "effectiveDateTime": "2024-01-01T00:00:00",
    "expiryDateTime": "2025-09-25T00:00:00"
  }
]
```
- `includeHistory=false`(기본) 이면 최신 버전만 반환.

## 4. 약관 생성
```http
POST /v1/admin/terms
Content-Type: application/json

{
  "termCode": "TERM_OF_MARKETING",
  "title": "마케팅 수신 동의",
  "type": "OPTIONAL",
  "displayOrder": 4,
  "status": "ACTIVE",
  "version": 1,
  "content": "새로운 약관 내용...",
  "effectiveDate": "2025-09-20T00:00:00",
  "expiryDate": null
}
```
```json
{
  "termId": 4,
  "termVersionId": 8
}
```
- 서버는 동일 `termCode` 존재 여부를 검증하고, 최초 버전은 자동으로 `is_current = true`로 설정한다.

## 5. 버전 추가
```http
PUT /v1/admin/terms/{termCode}/versions

{
  "baseVersion": 2,
  "content": "개정된 본문...",
  "effectiveDate": "2025-10-01T00:00:00",
  "expiryDate": null
}
```
성공 시
```json
{
  "version": 3,
  "termVersionId": 12
}
```
- `baseVersion`은 현재 활성 버전과 일치해야 하며, 불일치 시 `409 VERSION_CONFLICT`를 반환해 동시 수정 문제를 방지한다.

## 6. 메타데이터 수정
```http
PATCH /v1/admin/terms/{termCode}

{
  "title": "서비스 이용 약관(개정)",
  "displayOrder": 90,
  "status": "ACTIVE"
}
```
- 약관 코드를 변경할 수는 없으며, 제목/정렬/타입/상태만 수정 가능하다.
