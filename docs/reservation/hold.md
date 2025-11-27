# Reservation Hold (Redis)

임시예약(가계약) 정보를 Redis 해시 + ZSET 인덱스로 관리하는 구조 정리.

## 키 설계
- 해시 키: `reservation:hold:{roomTypeId}:{checkIn}:{checkOut}:{userId}` (`RedisKey.HOLD_HASH_FORMAT`)
- 인덱스 키(ZSET): `reservation:hold:index` (`RedisKey.HOLD_INDEX`)  
  - score: `expiredAt` epoch milli

### 해시 필드 (`RedisKey.Field`)
- `qty`: 해당 유저가 구간에 홀드한 수량 (`HINCRBY`)
- `roomTypeId`, `checkIn`, `checkOut`, `userId`: 메타(putIfAbsent)
- `createdAt`: 최초 생성 시각(putIfAbsent, ISO 문자열)
- `updatedAt`: 마지막 업데이트 시각
- `expiredAt`: 만료 예정 시각(슬라이딩 윈도우 기준)

## 생성 흐름 (ReservationHoldService.createHold)
1. `expiredAt = now + 10분` (HOLD_EXPIRE_WINDOW)
2. Redis 트랜잭션(MULTI/EXEC)으로 수행
   - `HINCRBY qty +1`
   - 메타 필드 `putIfAbsent`, `updatedAt` 갱신, `expiredAt` 덮어쓰기
   - `ZADD reservation:hold:index <expiredAtMillis> <holdKey>`
3. TTL은 사용하지 않음. 만료 처리와 재고 복구는 배치가 담당.

## 만료 처리 배치 (reservation-batch)
- 스케줄러: `@Scheduled(cron = "0 * * * * *")` 매 분 실행
- 서비스: `ReservationHoldExpireService.restoreExpiredHolds()`
  1) `ZRANGEBYSCORE hold:index 0 nowMillis LIMIT 0 N` 로 만료 후보 조회
  2) 각 키에 대해 해시 필드(`qty`, `roomTypeId`, `checkIn`, `checkOut`, `expiredAt`) 조회
  3) 아직 만료가 아니면 `ZADD`로 score 보정 후 스킵
  4) 만료면 RDB `RoomTypeStock`을 숙박 일수만큼 조회 후 `qty` 횟수만큼 `increase()`로 재고 복원
  5) Redis 정리: `DEL holdKey` + `ZREM hold:index holdKey` (트랜잭션)
- scan batch size: 기본 200개

## 주의사항 / TODO
- 배치 실패 시 재시도 및 모니터링 필요(예: dead-letter, 알람).
- `RoomTypeStock` 누락 시 경고 후 Redis 키를 정리만 함 → 데이터 보정 절차 별도 검토.
- `expiredAt` 보정 로직: 만료 연장 시 score 업데이트 필요(현재는 createHold에서만 설정).
- 배치 성능: 만료 후보가 많아지면 스캔 크기/샤딩/워크 큐 분리가 필요할 수 있음.
