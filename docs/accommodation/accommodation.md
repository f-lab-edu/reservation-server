# 숙소 (Accommodation)

## 요구사항

### 숙소 등록

- 숙소는 공급자(supplier)가 등록할 수 있다.
- 숙소 등록시 필요한 정보
    - 등록자: 공급자의 유저 ID
    - 기본 정보: 숙소 이름, 설명
    - 위치 정보: 주소 (위도, 경도 ?)
    - 숙소 상태: PENDING, APPROVED, REJECTED, SUSPENDED
    - 노출 상태: true/false (기본값: true)
    - 연락처 (전화번호)

### 숙소 상태 관리

- 숙소는 승인 상태를 가진다.
- 숙소을 등록하면 기본 상태는 `PENDING` 이다.
- 관리자는 숙소 등록을 검토하고 `APPROVED` 또는 `REJECTED` 상태로 변경할 수 있다.
- 관리자는 필요에 따라 `SUSPENDED` 상태로 변경할 수 있다.
- 숙소 상태 변경시 변경 이력을 기록한다.

**숙소 상태**

- `NONE`: 초기 상태 (등록 전, 히스토리 데이터에 처음 들어갈 때 사용)
- `PENDING`: 등록 후 관리자 승인 대기 상태
- `APPROVED`: 승인 완료, 서비스 노출 가능 (사용자 검색 시 노출)
- `REJECTED`: 승인 거절, 반려됨 (부적절한 콘텐츠 등)
- `SUSPENDED`: 관리자에 의해 일시 중지됨 (위반 사항 발생 시)

**숙소 노출 상태**

- `boolean` 필드로 관리하여 true/false로 노출 여부 결정
- true: 사용자 검색시 노출 및 예약 가능
- false: 사용자 검색시 노출되지 않으며 예약할 수 없음

## 숙소 조회 (공급자용)

- 공급자는 자신의 숙소를 조회할 수 있다. 

## 숙소 검색

- 사용자는 `APPROVED` 상태이면서 노출 상태가 `true`인 숙소에 대해 검색을 할 수 있다.
- 숙소 이름, 주소를 기반으로 검색하도록 한다. (LIKE 검색, FULLTEXT 검색 등)
- 검색 결과는 페이징 처리하여 반환한다.

## 테이블 설계

```sql
CREATE TABLE accommodations
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id    BIGINT       NOT NULL,
    name           VARCHAR(255) NOT NULL,
    description    TEXT         NOT NULL,
    address        VARCHAR(500) NOT NULL,
    status         VARCHAR(50)  NOT NULL, # PENDING, APPROVED, REJECTED, SUSPENDED
    is_visible     BOOLEAN      NOT NULL DEFAULT TRUE,
    contact_number VARCHAR(20),           # 숙소 연락처
    created_at     DATETIME     NOT NULL DEFAULT NOW(),
    updated_at     DATETIME     NOT NULL DEFAULT NOW() ON UPDATE NOW(),

    INDEX idx_status_visible (status, is_visible),
    FOREIGN KEY (supplier_user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```

```sql
CREATE TABLE accommodation_status_histories
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    accommodation_id BIGINT      NOT NULL,
    previous_status  VARCHAR(50) NOT NULL,
    new_status       VARCHAR(50) NOT NULL,
    reason           TEXT,                 # 상태 변경 사유
    changed_by       BIGINT      NOT NULL, # 관리자 유저 ID
    changed_at       DATETIME    NOT NULL DEFAULT NOW(),

    FOREIGN KEY (accommodation_id) REFERENCES accomodations (id),
    FOREIGN KEY (changed_by) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```

**숙소 상태 전이 흐름**

```
NONE → PENDING → APPROVED
               → REJECTED → (재심사) → PENDING 
               → SUSPENDED → (재승인) → APPROVED
```

**전이 규칙**

- 숙소 최초 등록: `NONE` → `PENDING` (자동)
- 관리자 승인: `PENDING` → `APPROVED`
- 관리자 반려: `PENDING` → `REJECTED`
- 재심사 요청: `REJECTED` → `PENDING` (공급자가 수정 후)
- 일시 중지: `APPROVED` → `SUSPENDED` (관리자 권한)
- 재승인: `SUSPENDED` → `APPROVED` (관리자 권한)

