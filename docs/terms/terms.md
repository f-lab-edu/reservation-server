# 약관(Terms)

## 1. 목적
회원 가입 및 서비스 이용 시 필수·선택 약관을 명확히 제시하고, 사용자가 동의한 버전을 추적하기 위한 도메인 정의다. 법적 요구사항을 충족하기 위해 약관 버전과 효력 기간을 관리해야 한다.

## 2. 요구사항
|항목|내용|
|---|---|
|관리 주체|관리자 페이지에서 약관 생성/수정/비활성화 수행|
|버전 관리|하나의 약관 코드에 대해 여러 버전이 존재. 새 버전 등록 시 effective date 지정|
|정렬|`display_order`로 노출 순서를 제어, 숫자가 작을수록 상단. 동일 값 시 ID ASC|
|사용자 제공|회원 가입 시 현재 시점 기준 `effective_date <= now < expiry_date` 이면서 `is_current = true`인 버전만 표시|
|동의 저장|사용자가 선택한 약관/버전 정보를 `user_term_agreements`에 저장|
|상태|`ACTIVE/INACTIVE`로 약관 코드 자체의 사용 여부를 관리|

## 3. 데이터 모델
```sql
CREATE TABLE terms (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    code          VARCHAR(30)  NOT NULL UNIQUE,
    title         VARCHAR(100) NOT NULL,
    type          VARCHAR(30)  NOT NULL,        -- REQUIRED / OPTIONAL
    display_order INT          NOT NULL,
    status        VARCHAR(10)  NOT NULL,        -- ACTIVE / INACTIVE
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_terms_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE term_versions (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    term_id       BIGINT    NOT NULL,
    version       INT       NOT NULL,
    content       TEXT      NOT NULL,
    is_current    BOOLEAN   NOT NULL DEFAULT FALSE,
    effective_date TIMESTAMP NULL,
    expiry_date    TIMESTAMP NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_term_version (term_id, version),
    INDEX idx_term_current (term_id, is_current),
    FOREIGN KEY (term_id) REFERENCES terms (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 4. 동의 내역 저장
```sql
CREATE TABLE user_term_agreements (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL,
    term_id         BIGINT NOT NULL,
    term_version_id BIGINT NOT NULL,
    agreed_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (term_id) REFERENCES terms (id),
    FOREIGN KEY (term_version_id) REFERENCES term_versions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
- 가입 시점에 선택한 약관 ID/버전을 그대로 저장해 추후 법적 분쟁 시 근거로 활용한다.
- 사용자가 약관을 재동의할 경우 새로운 레코드를 추가하며, 기존 데이터는 보존한다.

## 5. 운영 플로우
1. **새 약관 도입**: 관리자 페이지에서 `terms` 레코드를 생성하고 최초 버전을 추가한다.
2. **개정**: 기존 `term_id` 아래 새로운 `version`을 추가하고 `effective_date`를 설정. 과거 버전의 `is_current`는 false로 갱신한다.
3. **노출**: 사용자 API는 현재 시점 기준 노출 가능한 버전을 정렬 순서대로 전달한다.
4. **이력 확인**: 특정 사용자에 대해 `user_term_agreements`를 조회하면 언제 어떤 버전에 동의했는지 추적할 수 있다.
