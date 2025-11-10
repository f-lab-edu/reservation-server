# 숙소(Accommodation)

## 1. 목적
공급자가 등록한 숙소를 검수하고 노출 상태를 제어하기 위한 도메인 정의이다. 고객 검색 결과에는 승인된 숙소만 등장해야 하며, 공급자는 자신의 숙소 현황을 실시간으로 파악할 수 있어야 한다.

## 2. 주요 시나리오
- **등록**: 공급자가 기본 정보·위치·연락처를 입력하면 숙소는 `PENDING` 상태로 생성된다.
- **검수**: 관리자가 내용을 확인 후 `APPROVED` 또는 `REJECTED`로 변경한다. 재검토 요청 시 공급자가 수정 후 다시 제출한다.
- **노출 제어**: 서비스 정책 위반 혹은 운영 이슈가 있을 경우 `SUSPENDED`로 전환하고 검색 노출을 중지한다. 검색 가능 조건은 `status = APPROVED` & `is_visible = true`.
- **이력 관리**: 모든 상태 변경은 이유, 처리자, 변경 시각을 `accommodation_status_histories`에 기록해 감사 추적성을 확보한다.

## 3. 요구사항 요약
|구분|내용|
|---|---|
|등록 권한|공급자 계정만 가능, API 호출 시 JWT의 supplierId와 요청 숙소 소유자 검증|
|검색 조건|고객: 승인+노출 숙소만 조회. 공급자: 자신이 소유한 모든 숙소 조회|
|상태|`NONE`(이력용), `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED`|
|노출 플래그|Boolean, 기본값 `true`. 단 상태가 승인되어야 고객 노출 가능|
|정렬/검색|이름, 주소 LIKE 혹은 향후 Fulltext 확장; 결과는 페이징|

## 4. 데이터 모델
```sql
CREATE TABLE accommodations (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id    BIGINT       NOT NULL,
    name           VARCHAR(255) NOT NULL,
    description    TEXT         NOT NULL,
    address        VARCHAR(500) NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    is_visible     BOOLEAN      NOT NULL DEFAULT TRUE,
    contact_number VARCHAR(20),
    latitude       DECIMAL(10, 7) NULL,
    longitude      DECIMAL(10, 7) NULL,
    created_at     DATETIME     NOT NULL DEFAULT NOW(),
    updated_at     DATETIME     NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    INDEX idx_status_visible (status, is_visible),
    FOREIGN KEY (supplier_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE accommodation_status_histories (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    accommodation_id BIGINT      NOT NULL,
    previous_status  VARCHAR(20) NOT NULL,
    new_status       VARCHAR(20) NOT NULL,
    reason           TEXT,
    changed_by       BIGINT      NOT NULL,
    changed_at       DATETIME    NOT NULL DEFAULT NOW(),
    FOREIGN KEY (accommodation_id) REFERENCES accommodations (id),
    FOREIGN KEY (changed_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 5. 상태 전이
```
NONE → PENDING → APPROVED
               → REJECTED → (공급자 수정) → PENDING
APPROVED → SUSPENDED → (재승인) → APPROVED
```
- 최초 등록 시 `NONE → PENDING`은 자동 이행.
- 관리자만 `APPROVED/REJECTED/SUSPENDED`를 설정하며 사유 필드 필수.
- 고객 검색 API는 `status = APPROVED`이면서 `is_visible = true` 조건을 강제한다.

## 6. 권한 및 감사
- 공급자는 자신 소유 숙소에 대해서만 CRUD 가능하며, 요청 시 JWT의 subject와 숙소 `supplier_id`를 비교한다.
- 관리자 변경은 모두 `accommodation_status_histories`에 기록되며, 대시보드에서 마지막 상태 변경자와 사유를 노출한다.
