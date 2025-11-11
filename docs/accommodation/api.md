# 숙소 API 명세

## 1. 엔드포인트 요약
|분류|Method / Path|설명|권한|
|---|---|---|---|
|공급자|`POST /v1/hotels`|숙소 등록|`SUPPLIER`|
|공급자|`GET /v1/hotels`|자신이 등록한 숙소 목록 조회|`SUPPLIER`|
|공급자|`GET /v1/hotels/{id}`|숙소 상세 조회|`SUPPLIER` (소유자)|
|사용자|`GET /v1/public/hotels`|검색/목록 조회 (페이징)|익명/`USER`|
|사용자|`GET /v1/public/hotels/{id}`|승인 숙소 상세|익명/`USER`|

## 2. 숙소 등록 (공급자)
```http
POST /v1/hotels
Authorization: Bearer {supplier_access_token}
Content-Type: application/json

{
  "name": "세인트존스 호텔",
  "description": "강문해변 앞에 자리 잡은 시그니처 오션뷰 리조트",
  "address": "강원특별자치도 강릉시 강문동 1-1",
  "contactNumber": "033-660-9593",
  "isVisible": true
}
```
- 응답: `201 Created` + `{ "id": 1, "status": "PENDING" }`
- `supplierId`는 Access Token에서 추출하며 서버가 강제로 매핑한다.

## 3. 공급자 숙소 목록
```http
GET /v1/hotels?page=0&size=20
Authorization: Bearer {supplier_access_token}
```
```json
{
  "content": [
    {
      "id": 1,
      "name": "세인트존스 호텔",
      "status": "APPROVED",
      "isVisible": true,
      "createdAt": "2025-10-15T22:00:00Z",
      "updatedAt": "2025-10-20T05:10:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 3
}
```
- 공급자 자신이 소유한 숙소만 반환하며, 소유권 검증 실패 시 403을 리턴한다.

## 4. 사용자 검색/조회
```http
GET /v1/public/hotels?query=호텔&sort=rating,DESC&page=0&size=10
```
```json
{
  "content": [
    {
      "id": 1,
      "name": "세인트존스 호텔",
      "description": "오션뷰 객실",
      "address": "강원특별자치도 강릉시 강문동 1-1",
      "contactNumber": "033-123-4567"
    }
  ],
  "totalElements": 124
}
```
- 내부적으로 `status = APPROVED AND is_visible = true` 조건을 강제한다.
- 추후 위경도 기반 필터와 가격 필터를 query 파라미터로 확장한다.

### 상세 조회
```http
GET /v1/public/hotels/{id}
```
`404`는 존재하지 않거나 아직 승인되지 않은 숙소에 대한 접근을 의미한다.
