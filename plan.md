# plan.md — 가계약(v2) (회원당 동일 구간 1개 홀드)

## 1. 목표
- 회원별 `roomTypeId + checkIn + checkOut` 조합에 활성 홀드 1개만 유지.
- `clientHoldKey`(멱등 토큰)로 동일 구간 반복 클릭을 기존 가계약으로 귀속.
- `qty` 제거(존재=1). 가용성 계산은 `reservedCount + holdCount` 기반.
- Redis I/O 최소화: 해시 필드 다건 대신 JSON 단일 값 `SET ... EX`.
- 실시간 가용성 노출: 일자별 `hold-count` 누적 키를 사용하고 TTL로 자연 만료.
- v1 배치/ZSET 의존 제거, TTL 기반으로 정합성 확보.
- KEYS/SCAN 금지: 조회 시 stayDays 기반 결정적 키 생성 + MGET/파이프라인 사용.

## 2. 아키텍처 결정
- 키 설계
    - `hold:{holdId}`: JSON 값, 필드(roomTypeId, checkIn, checkOut, userId, clientHoldKey, createdAt, expiredAt),
      `SET ... EX <ttl>`.
    - `hold:idx:{userId}:{roomTypeId}:{checkIn}:{checkOut}` → holdId (`SETNX`, EX ttl) : 동일 구간 단일 가계약 보장.
    - `hold:snapshot:{clientHoldKey}:{roomTypeId}:{checkIn}:{checkOut}` → holdId (`SETNX`, EX ttl) : 멱등/중복 클릭 합치기. 다른
      hold에 묶여 있으면 409.
  - `hold-count:{roomTypeId}:{date}`: `INCRBY/DECRBY 1`, EX ttl(+버퍼).
    - 해시 대안(비권장): `hold-count:{roomTypeId}` 해시에 field=date로 누적하면 필드 TTL이 없어 정리가 어렵다. 필수 시 ZSET+HDEL 배치/Lua 필요.
- 생성 플로우
  1) Redisson MultiLock(roomTypeId + stayDays) 획득.
  2) `hold:idx` 존재하면 JSON GET → 만료/사용자 검증 → TTL 연장 → `hold:snapshot`(clientHoldKey 범위 포함)을 동일 holdId로 `SETNX`(충돌 시
     409) → 기존 가계약 반환(hold-count 변동 없음).
  3) 없으면 가용성(`reserved + hold-count`) 검증 → holdId 생성 → `hold:{id}` JSON SET EX → `hold:idx`/`hold:snapshot` 세팅 → 각 날짜 `hold-count` INCRBY 1 + EX.
- 확정/취소
  - holdId로 JSON GET → 사용자/만료 검증 → `hold-count` DECRBY 1 → hold/hash/idx/snapshot 삭제 → confirm 시 `RoomTypeStock.reserve(..., 1)` + 예약 생성.
- TTL/동시성
  - hold/idx/snapshot TTL 동일. hold-count TTL은 hold TTL보다 길게(또는 동일+버퍼) 설정해 자연 소멸. MultiLock wait/lease는 1s/5s 유지(필요 시 조정).

## 3. 해야 할 일 (TODO)
- DTO/API
    - `CreateReservationHoldRequest`에 `clientHoldKey`(UUID) 필드 추가, `qty` 관련 제거.
    - 엔드포인트 네이밍: `/v1/holds`(생성), `/v1/holds/{holdId}/confirm`, `/v1/holds/{holdId}`(DELETE 취소).
  - Controller/문서/http 예제 스펙 반영.
- Redis/도메인 리팩터
  - `ReservationHoldService`를 JSON 저장/조회 구조로 변경, 해시/qty 삭제.
  - `hold:idx`/`hold:snapshot`/`hold-count` 처리 로직 구현. TTL 연장 포함.
  - `RedisRepository`에 JSON SET/GET 헬퍼 추가 또는 ValueSerializer 조정.
  - Confirm/Cancel 경로에서 hold-count DECR, 키 삭제 멱등성 확보.
- 가용성 계산
  - `RoomTypeService.getAvailability`에서 `hold-count` 누적 키 사용하도록 교체(현재 KEYS 스캔 제거).
  - hold-count TTL 정책 문서화 및 버퍼 적용.
  - 조회 시 stayDays 목록으로 `hold-count:{roomTypeId}:{date}` 키를 결정적으로 생성해 MGET/파이프라인으로 읽기(null은 0 처리).
- 배치/문서 정리
  - v1 만료 배치 경로 비활성화/삭제 여부 결정 후 코드/문서 정리.
  - `docs/reservation/hold-v2.md`에 최종 키 설계/흐름 반영.
- 에러/검증
  - snapshot 충돌 409, hold 만료 408, 락 타임아웃 409/429, 권한 오류 처리 명시.

## 4. 테스트 계획 (TDD)

- T1 새 `clientHoldKey`로 생성 시 가계약 생성, hold-count 각 일자 +1, TTL 설정 검증.
- T2 동일 구간 다른 `clientHoldKey` 재호출 → 동일 holdId 반환, hold-count 변동 없음, TTL 연장.
- T3 `clientHoldKey`가 다른 hold에 이미 매핑돼 있으면 409 반환.
- T4 동시 요청(동일 구간) 시 MultiLock으로 하나만 성공, 다른 요청 409/429.
- T5 확정(confirm) 시 hold-count -1, Reservation/RoomTypeStock 반영, 키 삭제.
- T6 취소(cancel) 시 hold-count -1, 키 삭제, 멱등 호출 보장.
- T7 TTL 만료 후 hold/hold-count 자연 소멸 → 가용성 회복 확인.
- T8 락 대기 초과 시 즉시 실패 응답 검증.
