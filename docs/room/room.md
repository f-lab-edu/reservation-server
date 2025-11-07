# 객실 (Room)

## 요구사항

- 각 객실은 숙소(Accommodation)에 속해야 한다.
- 공급자(Supplier)는 자신이 소유한 숙소에만 객실을 등록할 수 있다.
- 객실은 **타입**을 나타내며, 실제로 예약 가능한 **재고 수량**을 가진다.
    - 해당 시스템에서는 객실을 "객실 타입(`RoomType`)"과 "객실 호실(`RoomUnit`)"로 구분하여 설계한다.
- 객실 등록 시 필요한 정보
    - 소속 숙소: accommodation_id (외래키)
    - 기본 정보: 객실 타입 이름, 설명
    - 가격 정보: 1박 기본 가격 (BigDecimal)
    - 수용 정보: 기준 인원, 최대 인원
    - 재고 정보: 해당 타입의 총 객실 수
    - 이미지 정보: 썸네일 이미지 URL

## 계층 구조 설계

```
Accommodation (숙소)
  └── RoomType (객실 타입) - N개
      └── RoomUnit (객실 호실) - N개
```

### 객실 설계

1. 사용자 입장에서의 객실은 **객실 타입(RoomType)이다**
    - "디럭스 더블룸", "스위트룸" 등의 객실 타입 정보를 저장
    - 해당 타입의 총 객실 수(total_rooms)를 가짐
    - 실제 개별 객실을 의미하는 것이 아님

2. **RoomUnit** 으로 재고 관리
    - 날짜별로 예약 가능한 객실 수를 관리
    - 해당 날짜의 가격 정보 (동적 가격 책정 가능)
    - 예약 가능 수량 = 총 객실 수 - 예약된 수량

3. **Reservation으로 예약 관리**
    - 실제 예약 정보는 별도 테이블에서 관리
    - 예약 시 RoomInventory의 수량을 차감
    - 취소 시 RoomInventory의 수량을 복구

## 객실 유닛 상태 관리

**객실 유닛 상태**

- `AVAILABLE`: 예약 가능
- `OCCUPIED`: 투숙 중
- `MAINTENANCE`: 수리/점검 중 (예약 불가)
- `CLEANING`: 청소 중 (예약 불가)
- `OUT_OF_SERVICE`: 서비스 불가 (예약 불가)

> 상태는 자동으로 관리되거나 공급자가 직접 제어할 수 있음

## 권한 관리

### 공급자 권한

- 공급자는 자신이 소유한 숙소의 객실만 등록/조회/수정/삭제할 수 있다.
- API 호출 시 숙소의 `supplier_id`와 JWT 토큰의 사용자 ID를 비교하여 권한을 검증한다.
- 권한이 없는 경우 `403 FORBIDDEN` 에러를 반환한다.

## 테이블 설계

### rooms (객실 타입)

```sql
CREATE TABLE room_types
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    accommodation_id  BIGINT         NOT NULL,
    name              VARCHAR(100)   NOT NULL,
    description       TEXT           NOT NULL,
    standard_capacity INT            NOT NULL, # 표준 인원 수
    max_capacity      INT            NOT NULL, # 최대 인원 수
    base_price        DECIMAL(10, 2) NOT NULL, # 기본 요금
    total_room_count  INT            NOT NULL,
    thumbnail         VARCHAR(255)   NOT NULL, # 썸네일 이미지 URL
    created_at        DATETIME DEFAULT NOW(),
    updated_at        DATETIME DEFAULT NOW() ON UPDATE NOW(),
    FOREIGN KEY (accommodation_id) REFERENCES accommodations (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```

### room_inventories (날짜별 재고 관리)

```sql
CREATE TABLE room_units
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type_id BIGINT       NOT NULL,
    room_number  VARCHAR(20)  NOT NULL,
    status       VARCHAR(30)  NOT NULL,
    notes        VARCHAR(255) NULL, # 객실 유닛에 대한 추가 정보
    created_at   DATETIME DEFAULT NOW(),
    updated_at   DATETIME DEFAULT NOW() ON UPDATE NOW(),

    FOREIGN KEY (room_type_id) REFERENCES room_types (id),
    UNIQUE KEY uk_room_type_id_room_number (room_type_id, room_number)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```
