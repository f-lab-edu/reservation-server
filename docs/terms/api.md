# 약관 (Terms)

## 1. 약관 리스트 조회 (사용자용)

```http request
GET /v1/sign-up/terms

REQUEST:
N/A

RESPONSE:
{
    "terms": [
        {
            "termId": 1,
            "termCode": "TERM_OF_SERVICE",
            "version": 1,
            "title": "이용약관",
            "type": "REQUIRED",
            "content": "제 1조(목적) ... 제 2조(정의) ...",
            "displayOrder": 1
        },
        {
            "termId": 2,
            "termCode": "TERM_OF_AGE",
            "version": 1,
            "title": "만 14세 이상 확인 (필수)",
            "type": "REQUIRED",
            "content": "XXX는 만 14세 미만 아동의 서비스 이용을 제한... 법정대리인 동의없이 회원가입을 하는 경우 제한... ",
            "displayOrder": 2
        },
        {
            "termId": 3,
            "termCode": "TERM_OF_PERSONAL_INFORMATION",
            "version": 2,
            "title": "개인정보 수집 및 이용",
            "type": "OPTIONAL",
            "content": "XXX는 회원가입을 위해 아래와 같은 개인정보를 수집합니다... ",
            "displayOrder": 3
        }
        // ...
    ]
}		
```

- content 필드는 상세 약관 내용을 포함한다.
- type 필드가 `REQUIRED` 인 경우 title에 `(필수)` 문구가 붙는다.
- version 필드는 약관의 버전을 나타낸다.
    - 사용자 가입 시점에는 가장 최신 버전 or 활성화된 약관들만 제공하면 된다.

## 2. 약관 리스트 조회 (관리자용)

```http request
GET /v1/admin/terms

REQUEST:
N/A

RESPONSE:
[
  {
    "termId": 2,
    "termCode": "TERM_PRIVACY",
    "title": "개인정보 처리방침",
    "type": "REQUIRED",
    "displayOrder": 101,
    "status": "ACTIVE",
    "createdAt": "2025-09-25T05:32:21",
    "updatedAt": "2025-09-25T05:32:21",
    "termVersionId": 5,
    "version": 3,
    "isCurrent": true,
    "content": "개인정보 처리방침에 따라.. (최신 버전)",
    "effectiveDateTime": "2025-09-25T05:32:22",
    "expiryDateTime": null,
    "termVersionCreatedAt": "2025-09-25T05:32:22",
    "termVersionUpdatedAt": "2025-09-25T05:32:22"
  },
  {
    "termId": 1,
    "termCode": "TERM_SERVICE",
    "title": "서비스 이용 약관",
    "type": "REQUIRED",
    "displayOrder": 100,
    "status": "ACTIVE",
    "createdAt": "2025-09-25T05:32:21",
    "updatedAt": "2025-09-25T05:32:21",
    "termVersionId": 2,
    "version": 2,
    "isCurrent": true,
    "content": "본 서비스 이용 동의.. (최신 버전)",
    "effectiveDateTime": "2025-09-25T05:32:22",
    "expiryDateTime": null,
    "termVersionCreatedAt": "2025-09-25T05:32:22",
    "termVersionUpdatedAt": "2025-09-25T05:32:22"
  },
  {
    "termId": 1,
    "termCode": "TERM_SERVICE",
    "title": "서비스 이용 약관",
    "type": "REQUIRED",
    "displayOrder": 100,
    "status": "ACTIVE",
    "createdAt": "2025-09-25T05:32:21",
    "updatedAt": "2025-09-25T05:32:21",
    "termVersionId": 1,
    "version": 1,
    "isCurrent": false,
    "content": "본 서비스 이용 동의..",
    "effectiveDateTime": "2025-09-25T05:32:22",
    "expiryDateTime": null,
    "termVersionCreatedAt": "2025-09-25T05:32:22",
    "termVersionUpdatedAt": "2025-09-25T05:32:22"
  }
]
```

## 3. 약관 생성 API (관리자용)

```http request
POST /v1/admin/terms

REQUEST:
{
    "termCode": "TERM_OF_MARKETING",
    "title": "마케팅 수신 동의",
    "type": "REQUIRED" | "OPTIONAL",
    "displayOrder": 4,
    "isActive": true,
    "version": 1,
    "content": "새로운 약관 내용...",
    "effectiveDate": "2025-09-20T00:00:00",
    "expiryDate": null
}

RESPONSE:
{
    "termId": 4,
    "termVersionId": 8
}
```

## 4. 약관 버전 추가 API (관리자용)

```http request
PUT /v1/admin/terms/{termCode}/versions

REQUEST:
{
    "currentVersion": 1,    // 현재 활성화된 버전 (기존 버전)
    "content": "수정된 약관 내용...",
    "effectiveDate": "2025-10-01T00:00:00"
}

RESPONSE:
[성공]
{
    "success": true,
    "error": null
    "result": {
        "termVersionId": 9,
        "version": 2
    }
}

[실패 (충돌)]
{
    "success": false,
    "error": {
        "code": "VERSION_CONFLICT",
        "message": "현재 활성화된 버전과 일치하지 않습니다."
    }
}
```