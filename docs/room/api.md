# 객실 API 명세

## 1. 엔드포인트 요약
|분류|Method / Path|설명|권한|
|---|---|---|---|
|공급자|`POST /v1/supplier/accommodations/{accommodationId}/rooms`|객실 타입 등록 및 초기 RoomUnit 생성|`SUPPLIER`|
|공급자|`GET /v1/supplier/accommodations/{accommodationId}/rooms`|객실 타입 목록 조회|`SUPPLIER`|
|공급자|`GET /v1/supplier/accommodations/{accommodationId}/rooms/{roomTypeId}`|객실 타입 상세|`SUPPLIER`|
|공급자|`PATCH /v1/supplier/accommodations/{accommodationId}/rooms/{roomTypeId}`|가격/정원/노출 정보 수정|`SUPPLIER`|
|공급자|`POST /v1/supplier/rooms/{roomTypeId}/units`|호실(RoomUnit) 추가|`SUPPLIER`|
|사용자|`GET /v1/public/hotels/{hotelId}/rooms`|노출 가능한 객실 타입 목록|익명/`USER`|
|사용자|`GET /v1/public/rooms/{roomTypeId}`|객실 타입 상세 + 실시간 가용 정보|익명/`USER`|

## 2. 객실 타입 등록
```http
POST /v1/supplier/accommodations/{accommodationId}/rooms
Authorization: Bearer {supplier_access_token}
Content-Type: application/json

{
  "name": "디럭스 더블룸",
  "description": "오션뷰 + 킹베드 + 프리미엄 어메니티",
  "basePrice": 150000,
  "standardCapacity": 2,
  "maxCapacity": 3,
  "totalRoomCount": 10,
  "thumbnail": "https://cdn.example.com/rooms/deluxe-double.jpg",
  "roomNumbers": ["101", "102", "103", "104", "105", "106", "107", "108", "109", "110"]
}
```
- 응답: `201 Created` + `{ "roomTypeId": 1 }`
- 전달된 `roomNumbers` 만큼 RoomUnit을 생성한다. 누락 시 공급자가 추후 유닛 추가 API를 호출해야 한다.

## 3. 객실 타입 목록 조회
```http
GET /v1/supplier/accommodations/{accommodationId}/rooms?page=0&size=20
Authorization: Bearer {supplier_access_token}
```
```json
{
  "content": [
    {
      "id": 1,
      "name": "디럭스 더블룸",
      "standardCapacity": 2,
      "maxCapacity": 3,
      "totalRoomCount": 10,
      "basePrice": 150000,
      "thumbnail": "https://cdn.example.com/rooms/deluxe-double.jpg",
      "status": "SELLING"
    }
  ],
  "totalElements": 4
}
```
- 응답에는 공급자 관리를 위한 내부 필드(판매 상태, 비고 등)를 포함하고, RoomUnit 요약(가용/전체) 정보를 함께 내려 리포팅에 활용한다.

## 4. 객실 타입 상세 조회 및 수정
```http
GET /v1/supplier/accommodations/{accommodationId}/rooms/{roomTypeId}
Authorization: Bearer {supplier_access_token}
```
```json
{
  "id": 1,
  "name": "디럭스 더블룸",
  "description": "오션뷰 + 킹베드",
  "basePrice": 150000,
  "standardCapacity": 2,
  "maxCapacity": 3,
  "totalRoomCount": 10,
  "thumbnail": "https://cdn.example.com/rooms/deluxe-double.jpg",
  "roomUnits": [
    {"unitId": 11, "roomNumber": "101", "status": "AVAILABLE"},
    {"unitId": 12, "roomNumber": "102", "status": "MAINTENANCE"}
  ]
}
```

수정 예시는 다음과 같다.
```http
PATCH /v1/supplier/accommodations/{accommodationId}/rooms/{roomTypeId}
Content-Type: application/json

{
  "basePrice": 165000,
  "standardCapacity": 3,
  "thumbnail": "https://cdn.example.com/rooms/deluxe-double_v2.jpg"
}
```

## 5. RoomUnit 관리
```http
POST /v1/supplier/rooms/{roomTypeId}/units

{
  "roomNumbers": ["1201", "1202"],
  "defaultStatus": "AVAILABLE"
}
```
- RoomUnit 상태 변경은 `PATCH /v1/supplier/room-units/{unitId}` 엔드포인트에서 처리하며, 상태 전환 시 사유를 남긴다.

## 6. 사용자 노출 API
```http
GET /v1/public/hotels/{hotelId}/rooms?checkIn=2025-05-01&checkOut=2025-05-03&guests=3
```
```json
{
  "rooms": [
    {
      "id": 1,
      "name": "디럭스 더블룸",
      "description": "오션뷰 + 킹베드",
      "basePrice": 180000,
      "maxCapacity": 3,
      "thumbnail": "https://cdn.example.com/rooms/deluxe-double.jpg",
      "availableCount": 4
    }
  ]
}
```
- 내부적으로 재고 서비스에서 날짜별 가용 수량을 조회하여 `availableCount`에 반영한다.
