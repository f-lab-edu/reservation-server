# 숙소 (Accommodation)

## 1. 호텔 등록 (공급자용)

```http request
POST /v1/hotels
Authorization: Bearer {supplier_access_token} # 해당 값을 기반으로 supplierId 추출

REQUEST:
{
    "name": "세인트존스 호텔",
    "description": "강문해변 앞에 자리 잡아 객실에서 드넓고 아름다운 바다를 감상할 수 있습니다.\n아름다운 대자연과 어우러지는 특별하고도 환상적인 경험을 느낄 수 있습니다."
    "address": "강원특별자치도 강릉시 강문동 1-1",
    "contact_number": "033-660-9593",
    
    "is_visible": true # 필요한가? PENDING -> APPROVED 되는 시점에 true로 바뀌는게 맞지 않나?
}
```

## 2. 호텔 조회 (공급자용)

> 자신이 등록한 호텔 조회 (N개의 호텔이 있을 수 있음)

```http request
GET /v1/hotels
Authorization: Bearer {suuplier_access_token}

REQUEST:
N/A

RESPONSE:
[
    {
        "id": 1,
        "name": "세인트존스 호텔",
        "description": "강문해변 앞에 자리 잡아 객실에서 드넓고 아름다운 바다를 감상할 수 있습니다.",
        "address": "강원특별자치도 강릉시 강문동 1-1",
        "contact_number": "033-660-9593",
        "status": "APPROVED",
        "is_visible": true,
        "created_at": "2025-10-15T22:00:00Z",
        "updated_at": "2025-10-15T22:00:00Z"
    },
...
]

```

## 3. 호텔 검색 (사용자용)

> 추후에 페이징 처리 진행 

```http request
GET /v1/hotels?query=호텔

REQUEST:
N/A

RESPONSE:
[
    {
        "id": 1,
        "name": "세인트존스 호텔",
        "description": "강문해변 앞에 자리 잡아 객실에서 드넓고 아름다운 바다를 감상할 수 있습니다.",
        "address": "강원특별자치도 강릉시 강문동 1-1",
        "contact_number": "033-123-4567",
    },
    {
        "id": 2,
        "name": "여수 라마다 프라자 호텔",
        "description": "특 1급 프리미엄 호텔에서 여수의 아름다운 바다를 한눈에 볼 수 있는 인피니티 풀과, 국내 최고 높이의 짚트랙을 동시에 즐기세요",
        "address": "전남 여수시 돌산읍 우두리 1048-4",
        "contact_number": "061-123-1234",
    }
]

```