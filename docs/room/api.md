# 객실 API

## 1. 객실 타입 등록 (공급자용)

공급자가 자신의 숙소에 객실 타입을 등록합니다.

```http request
POST /v1/supplier/accommodations/{accommodationId}/rooms
Authorization: Bearer {supplier_access_token}
Content-Type: application/json

REQUEST:
{
    "name": "디럭스 더블룸",
    "description": "넓고 쾌적한 디럭스룸으로 오션뷰를 감상할 수 있습니다. 킹사이즈 침대와 프리미엄 어메니티가 제공됩니다.",
    "basePrice": 150000,
    "standardCapacity": 2,
    "maxCapacity": 3,
    "totalRooms": 10,
    "thumbnail": "https://example.com/rooms/deluxe-double.jpg",
    "roomNumber": ["101", "102", "103", "104", "105", "106", "107", "108", "109", "110"]
}

RESPONSE: (201 Created)
{
    "code": 0,
    "message": null
    "content": {
        "roomTypeId": 1
    }
}
```

### 필드 설명

- `name`: 객실 타입 이름 (예: "디럭스 더블룸", "스위트룸")
- `description`: 객실 상세 설명
- `basePrice`: 1박 기본 가격 (원 단위)
- `standardCapacity`: 기준 인원 (추가 인원 없을 때 수용 가능 인원)
- `maxCapacity`: 최대 인원 (추가 인원 포함 최대 수용 가능 인원)
- `totalRoomCount`: 해당 타입의 총 객실 수
- `thumbnail`: 대표 이미지 URL
- `roomNumbers`: 객실 번호 (객실 유닛 엔티티 생성을 위한 배열)

## 2. 숙소의 객실 타입 목록 조회 (공급자용)

공급자가 자신의 숙소에 등록된 모든 객실 타입 목록을 조회합니다.

```http request
GET /v1/supplier/accommodations/{accommodationId}/rooms
Authorization: Bearer {supplier_access_token}

REQUEST:
N/A

RESPONSE: (200 OK)
{
  "code": 0,
  "message": null,
  "content": [
    {
      "id": 1,
      "name": "디럭스 더블룸",
      "description": "넓고 쾌적한 디럭스룸으로 오션뷰를 감상할 수 있습니다. 킹사이즈 침대와 프리미엄 어메니티가 제공됩니다.",
      "standardCapacity": 2,
      "maxCapacity": 3,
      "totalRoomCount": 6,
      "basePrice": 150000.00,
      "thumbnail": "https://example.com/rooms/deluxe-double.jpg"
    },
    {
      "id": 2,
      "name": "디럭스 더블룸",
      "description": "넓고 쾌적한 디럭스룸으로 오션뷰를 감상할 수 있습니다. 킹사이즈 침대와 프리미엄 어메니티가 제공됩니다.",
      "standardCapacity": 2,
      "maxCapacity": 3,
      "totalRoomCount": 6,
      "basePrice": 150000.00,
      "thumbnail": "https://example.com/rooms/deluxe-double.jpg"
    }
  ]
}
```

## 3. 특정 객실 타입 상세 조회 (공급자용)

공급자가 자신의 숙소에 등록된 특정 객실 타입의 상세 정보를 조회합니다.

```http request
GET /v1/supplier/accommodations/{accommodationId}/rooms/{roomId}
Authorization: Bearer {supplier_access_token}

REQUEST:
N/A

RESPONSE: (200 OK)
{
    "success": true,
    "data": {
        "id": 1,
        "accommodationId": 100,
        "name": "디럭스 더블룸",
        "description": "넓고 쾌적한 디럭스룸으로 오션뷰를 감상할 수 있습니다. 킹사이즈 침대와 프리미엄 어메니티가 제공됩니다.",
        "basePrice": 150000,
        "standardCapacity": 2,
        "maxCapacity": 3,
        "totalRooms": 10,
        "thumbnail": "https://example.com/rooms/deluxe-double.jpg",
        "status": "SELLING"
    },
    "error": null
}
```

