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

## 3.1 Redis 구조 (TTL 기반, JSON)

- `hold:{holdId}`: JSON 값(roomTypeId, checkIn, checkOut, userId, clientHoldKey, createdAt, expiredAt), `SET ... EX 600`(
  10분).
- `hold:idx:{userId}:{roomTypeId}:{checkIn}:{checkOut}` → holdId (`SETNX`, EX 10분) : 동일 회원/구간 단일 가계약 보장.
- `hold:snapshot:{clientHoldKey}:{roomTypeId}:{checkIn}:{checkOut}` → holdId (`SETNX`, EX 10분) : 멱등/중복 클릭 합치기, 충돌 시 409.
- `hold-count:{roomTypeId}:{date}`: `INCRBY/DECRBY 1` + `EXPIRE 600`. 값이 0 이하가 되면 즉시 삭제.
- KEYS/SCAN 미사용: stayDays로 결정적 키를 만들어 MGET/파이프라인 조회.

재호출 정책: 동일 회원·구간 요청이 오면 기존 가계약을 삭제(hold-count DECR→DEL, 키 삭제) 후 새로 생성한다. 기존 holdId로 결제 시 만료/삭제 응답 처리.

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

| 항목       | v1 (배치)           | v2 (실시간)                    |
|----------|-------------------|-----------------------------|
| 가계약 저장   | Redis Hash + ZSET | Redis JSON + 인덱스/스냅샷 키(TTL) |
| 가계약 만료   | 배치 처리             | TTL 자동 만료                   |
| 재고 차감    | 가계약 생성 시 재고 차감    | 확정 예약만 반영                   |
| 재고 계산    | RoomTypeStock만 기준 | total - reserved - hold     |
| 정합성      | 배치 실패 시 위험        | 실시간 계산으로 정합성 상승             |
| Redis 역할 | 가계약 + 인덱스         | 가계약(임시 저장)                  |
| 아키텍처 복잡도 | 높음                | 간결                          |

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

# 6. 구현 체크리스트 (v2)

1. DTO/API: `clientHoldKey` 필수(UUID), qty 제거. 엔드포인트 `/v1/holds`, `/v1/holds/{holdId}/confirm`,
   `DELETE /v1/holds/{holdId}`.
2. Redis 접근: JSON `SET ... EX 600`, idx/snapshot `SETNX` + EX, hold-count INCR/DECR + EXPIRE 600(0 이하면 DEL).
3. 재호출 처리: 동일 회원·구간 요청 시 기존 가계약 삭제 후 새로 생성. 멱등 토큰 충돌 시 409.
4. 가용성 조회: stayDays로 `hold-count:{roomTypeId}:{date}` 키 생성 후 MGET/파이프라인. KEYS/SCAN 금지.
5. 락: roomType+숙박일 MultiLock(대기/보유 1s/5s 유지).
6. 에러 매핑: HOLD_EXPIRED, HOLD_NOT_FOUND, HOLD_KEY_CONFLICT(409), LOCK_TIMEOUT(409/429), FORBIDDEN(403) 등 명시.
7. 테스트: 생성/재호출/충돌, 동시성(락), 확정/취소 DECR/DEL, TTL 만료 후 가용성 복원, KEYS 미사용 검증.

---

# 7. 멱등 토큰(clientHoldKey) 정책

- 생성 주체: **클라이언트가 UUID(v4) 또는 ULID**로 생성하고 요청에 포함한다. 서버는 형식 검증만 수행한다.
- 스코프: 키는 구간을 포함해 매핑한다. 예) `hold:snapshot:{clientHoldKey}:{roomTypeId}:{checkIn}:{checkOut}` → holdId.
- 규칙:
    - 동일 `clientHoldKey+구간` 재호출은 기존 가계약을 반환하거나(멱등) 정책에 따라 기존 가계약 삭제 후 재생성. 현재는 **삭제 후 재생성**으로 결정.
    - 다른 가계약에 이미 매핑된 `clientHoldKey+구간` 조합이면 409(CONFLICT).
    - 토큰 누락/포맷 오류는 400(BAD_REQUEST).
- 시퀀스 기반 발급은 불필요: TTL 데이터 특성상 DB 시퀀스/epoch 조합을 쓰지 않고 난수형 키로 충분하며, 오버헤드와 단일 발급 지점을 없앤다.***

---

# 8. 예외/정합성 정책

- 락 획득 실패: 409(CONFLICT) 고정.
- 중복 confirm/cancel: `DECRBY` 결과가 0 이하 등 이미 처리된 가계약이면 409(CONFLICT) + `RESERVATION_HOLD_ALREADY_PROCESSED`(또는 동등 코드) 반환.
- 스테일 키(hold:idx/snapshot는 남았지만 hold:{id} 없음):
    - 오류 반환(404 NOT_FOUND 또는 EXPIRED 취급) 후 idx/snapshot 키를 즉시 정리해 다음 요청이 깨끗이 처리되도록 한다.
- 멱등 토큰 충돌: `clientHoldKey+구간`이 다른 가계약에 매핑되어 있으면 409(CONFLICT) + `RESERVATION_HOLD_KEY_CONFLICT`.
- 권한 위반: userId 불일치 시 403(FORBIDDEN).
- 만료: expiredAt < now면 400/408(`RESERVATION_HOLD_EXPIRED`).
