# 숙소 최저가 Snapshot 전략 (TODO 메모)

## 1. 배경
- 현재 검색 쿼리는 `accommodations` 와 `room_types` 를 조인한 뒤 `MIN(rt.base_price)` 를 구해 페이징/필터를 수행한다.
- 체크인·체크아웃, 수용 인원, 가용 객실 조건까지 합쳐지면 그룹바이 쿼리가 복잡해지고, `room_types` 가 많은 숙소일수록 응답 지연이 발생한다.
- `LIMIT/OFFSET` 기반 페이지네이션을 사용할 때도, 집계 비용 때문에 커버링 인덱스만으로는 충분히 최적화하기 어렵다.

## 2. 해결 아이디어: 읽기 전용 Snapshot 테이블
```
accommodation_price_snapshot
----------------------------
accommodation_id  BIGINT PK/FK
min_price         DECIMAL(10,2)
collected_at      DATETIME
source            VARCHAR (optional: base / promo 등 태그)
```

- 검색 시 `accommodations` + `accommodation_price_snapshot` 만 조인해서 바로 최저가를 가져온다.
- `room_types` 는 쓰기 경로(등록/수정)에서만 참조하게 되어, 검색 트래픽과 분리된다.
- snapshot 갱신은 배치(주 1회 등) 또는 이벤트 기반 증분 계산(가격 변경 시 해당 숙소만 재계산)으로 나눌 수 있다.
- 프로모션/행사 가격이 있다면 별도 정책 테이블을 두어 snapshot 값보다 우선 적용하거나, snapshot 테이블에 `promo_min_price` 컬럼을 추가해 관리한다.

## 3. TODO
1. `accommodation_price_snapshot` 테이블 스키마 설계 및 마이그레이션 추가.
2. RoomType 가격/가용 여부 변경 이벤트 시 snapshot 을 재계산하는 서비스/배치 작성.
3. 검색 레포지토리에서 `room_types` 대신 snapshot 을 조인하도록 리팩터링.
4. snapshot 값과 실시간 계산 결과의 차이를 주기적으로 검증하는 모니터링 쿼리/대시보드 구성.

> **NOTE:** snapshot 전략은 읽기 성능을 확보하는 대신 최신성(latency)이 덜 민감하다는 가정이 필요하다. 행사/즉시 할인 같은 실시간 가격 정책은 별도 계층에서 처리하고, snapshot 은 기본가 기준의 빠른 응답을 제공하는 역할에 집중시킨다.
