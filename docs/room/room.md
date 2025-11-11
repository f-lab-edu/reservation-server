# 객실(Room)

## 1. 목적
숙소 단위에서 상품을 노출할 때 고객에게는 객실 타입을, 운영 측면에서는 실제 객실 유닛을 관리해야 한다. 본 문서는 두 레이어를 분리하여 재고, 가격, 상태를 일관성 있게 유지하는 방법을 설명한다.

## 2. 도메인 모델
```
Accommodation
 └─ RoomType (상품 단위)
     └─ RoomUnit (실제 호실/재고 단위)
```
- **RoomType**: 고객에게 노출되는 객실 유형. 이름, 설명, 기본 요금, 기준/최대 인원, 이미지 정보, 총 객실 수를 가진다.
- **RoomUnit**: 실제 호실 단위. 재고, 운영 상태, 객실 번호 등을 관리한다.
- **Reservation**: 예약 시 특정 날짜 범위에 대해 RoomUnit 또는 해당 타입의 가용 수량을 점유한다.

## 3. 요구사항 요약
|항목|내용|
|---|---|
|등록 권한|공급자는 자신이 소유한 숙소(`accommodation.supplier_id`)에만 RoomType/RoomUnit을 추가할 수 있다.|
|가격/재고|RoomType에 1박 기준 요금과 총 객실 수를 저장하고, Daily Inventory는 예약 서비스에서 별도 테이블로 관리한다.|
|노출 정보|RoomType에는 썸네일·대표 설명을 저장하며, 필터링은 기준/최대 인원, 가격 범위를 기반으로 한다.|
|검증|API 호출 시 JWT의 subject와 숙소의 `supplier_id`를 비교하여 권한을 확인한다.|

## 4. 상태 모델
### RoomUnit 상태
- `AVAILABLE`: 예약 가능
- `OCCUPIED`: 투숙 중
- `CLEANING`: 청소로 인해 임시 불가
- `MAINTENANCE`: 수리/점검
- `OUT_OF_SERVICE`: 장기 미사용

상태 전환은 예약 이벤트(체크인/체크아웃)와 공급자 수동 조작 두 경로를 지원한다. 상태 변경 시 감사 로그를 남겨 추후 운영 리포트에 활용한다.

## 5. 데이터 모델
```sql
CREATE TABLE room_types (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    accommodation_id  BIGINT         NOT NULL,
    name              VARCHAR(100)   NOT NULL,
    description       TEXT           NOT NULL,
    standard_capacity INT            NOT NULL,
    max_capacity      INT            NOT NULL,
    base_price        DECIMAL(10, 2) NOT NULL,
    total_room_count  INT            NOT NULL,
    thumbnail         VARCHAR(255)   NOT NULL,
    created_at        DATETIME       DEFAULT NOW(),
    updated_at        DATETIME       DEFAULT NOW() ON UPDATE NOW(),
    FOREIGN KEY (accommodation_id) REFERENCES accommodations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE room_units (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_type_id BIGINT      NOT NULL,
    room_number  VARCHAR(20) NOT NULL,
    status       VARCHAR(30) NOT NULL,
    notes        VARCHAR(255),
    created_at   DATETIME DEFAULT NOW(),
    updated_at   DATETIME DEFAULT NOW() ON UPDATE NOW(),
    UNIQUE KEY uk_room_type_room_number (room_type_id, room_number),
    FOREIGN KEY (room_type_id) REFERENCES room_types (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 6. 운영 원칙
1. **예약 가능 수량 계산**  
   `가용 객실 = total_room_count - (해당 날짜 예약 수 + 유지보수로 막힌 수량)`. 재고 계산은 배치/쿼리에 의존하지 않고 Reservation 서비스에서 실시간으로 관리한다.
2. **가격 정책**  
   기본 요금은 RoomType에 저장하고, 성수기/프로모션 가격은 별도 `room_type_prices` 또는 쿠폰 시스템에서 오버라이드한다.
3. **이미지/콘텐츠 관리**  
   썸네일 외 다중 이미지는 별도 `room_type_images` 테이블에 저장하며, 정렬 순서를 포함한다.
4. **삭제 정책**  
   활성 예약이 있는 RoomType/RoomUnit은 물리 삭제하지 않고 `is_active` 플래그나 상태를 통해 숨긴다.
