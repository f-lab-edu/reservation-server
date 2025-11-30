# v2 - 실시간 가계약(Reservation Hold) 처리 구조

기존 방식(v1): **가계약을 재고로 직접 반영하고, 만료 시 배치로 복구하는 구조**였다.

개선하려는 방식(v2): **가계약은 Redis TTL로 관리하고, 재고는 RDB 기반으로 실시간 계산하는 구조**로 전환한다.

이 문서는 v1의 문제점을 해결하기 위해 설계한 v2 구조를 정리한 것이다.

# 1. v1 구조 요약 (배치 기반)

### 동작 방식

- 가계약은 Redis Hash + ZSET으로 저장
- `expiredAt`은 ZSET에 score로 관리
- 가계약은 생성 시 **즉시 RoomTypeStock 재고 감소**
- 배치가 만료된 가계약을 스캔하여 재고 복구
- Redis TTL은 사용하지 않음

### 장점

- 구조가 직관적
- 가계약 → 재고 차감 → 배치 복구 흐름이 시각적으로 명확

### 단점

- 배치 실패 시 재고 오정합 발생
- Redis TTL을 쓰지 않아 운영 부담 증가
- 가계약 증가 시 배치 스캔 비용 증가
- 시스템이 실시간 재고가 아닌 “정산형 재고”가 됨
- 가계약이 많을수록 RoomTypeStock 수치 변동이 과도

---

# 2. v2 구조 요약 (실시간 가계약)

v2 구조의 핵심 아이디어는 다음과 같다.

> **가계약은 재고를 직접 변경하지 않는다.**  
> **재고는 조회 시 실시간 계산한다.**

### 실시간 가용 재고 공식

```
available = totalQuantity 
            - reservedCount (확정 예약 - RDB)
            - holdCount (Redis TTL 기반)
```

즉, RoomTypeStock은 더 이상 가계약 처리를 하지 않고 "확정된 예약에 대한 일별 재고 집계" 역할만 수행하며, Redis는 가계약의 생명주기 관리에만 집중한다.

---

# 3. v2 설계 상세

## 3.1 Redis 구조 (TTL 기반)

### Hash

- Key: `reservation:hold:{roomTypeId}:{checkIn}:{checkOut}:{userId}`
- TTL: 가계약 유지 시간 (예: 10분)
- Fields:
    - qty
    - roomTypeId
    - checkIn
    - checkOut
    - userId
    - createdAt
    - updatedAt

### 변화점

- **ZSET 인덱스 제거**: TTL이 자동으로 만료를 처리하므로 인덱싱 불필요.

## 3.2 RoomType / RoomTypeStock 책임 분리

### RoomType (정적 정보)

- 객실 타입의 메타 데이터
- ID, 이름, 기본 수량, 설명, 가격 등

### RoomTypeStock (동적 정보)

- `roomTypeId + targetDate` 단위로 존재
- **확정 예약 수(reservedCount)** 만 반영
- Daily inventory table 역할

### 재고 계산 시 사용하는 입력값

| 구분            | 출처            | 사용 목적       |
|---------------|---------------|-------------|
| totalQuantity | RoomType      | 객실 타입의 총 수량 |
| reservedCount | RoomTypeStock | 확정된 예약 수    |
| holdCount     | Redis         | 활성 가계약 수    |

---

# 4. v1 → v2 변화 요약

| 항목       | v1 (배치)           | v2 (실시간)                |
|----------|-------------------|-------------------------|
| 가계약 저장   | Redis Hash + ZSET | Redis Hash(TTL)         |
| 가계약 만료   | 배치 처리             | TTL 자동 만료               |
| 재고 차감    | 가계약 생성 시 재고 차감    | 확정 예약만 반영               |
| 재고 계산    | RoomTypeStock만 기준 | total - reserved - hold |
| 정합성      | 배치 실패 시 위험        | 실시간 계산으로 정합성 상승         |
| Redis 역할 | 가계약 + 인덱스         | 가계약(임시 저장)              |
| 아키텍처 복잡도 | 높음                | 간결                      |

# 5. 개선 효과

### 1) 배치 제거

- 스케줄러, 배치 모듈, 인프라 부담 제거
- 재고 오정합 가능성 감소

### 2) 실시간 재고 판단

- 사용자가 보는 재고 = 실제 재고
- 배치 주기에 따른 딜레이 없음

### 3) RDB와 Redis 역할 명확화

- RDB: 확정 예약의 단일 소스
- Redis: 가계약 TTL 기반 임시 저장소

### 4) 확장성과 유지보수성 증가

- Redis 키는 TTL만 관리하면 됨
- RoomTypeStock은 단순한 daily reservation counter가 됨

---

# 6. 앞으로의 TODO

1. **재고 조회 쿼리 최적화**
    - `reservedCount` + `holdCount` 조합을 빠르게 가져오는 쿼리 설계
    - Full scan 방지

2. **Redis 멀티키 holdCount 계산 전략 확정**
    - `roomTypeId:{date}` 단위로 hold 카운트 누계 저장 여부 검토

3. **FSM(상태머신) 도입 고려**
    - HOLDING → CONFIRMED → CANCELED → EXPIRED
    - 상태 전이를 명확히 정의하여 로직 안정성 강화

4. **ReservationHold TTL 정책 튜닝**
    - Device/UX 측에서 적절한 유지시간 검토

5. **MultiLock 유지 여부 검토**
    - 가계약이 재고를 직접 변경하진 않더라도  
      “특정 구간에 대한 가계약 중복 방지”는 여전히 중요할 수 있음.
