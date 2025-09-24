# 약관 (Terms)

## 요구사항

- 약관에 대한 관리는 관리자 페이지에서 수행한다.
    - 약관 생성, 수정, 삭제, 조회가 가능하다.
    - 약관 버전 관리가 필요하다. (새로운 버전 추가, 활성화/비활성화)
- 서비스의 회원가입 시 필수 및 선택 약관을 사용자에게 제공하고, 사용자가 동의한 약관 정보를 저장한다.
    - 저장을 할 때는 약관의 버전 정보도 필수적으로 함께 저장한다.
- 사용자 가입 시점에는 최신 버전의 약관만 확인하면 된다.
    - 즉, 사용자의 API 요청에는 최신 버전의 약관 정보만 제공한다.
- 약관의 정렬 순서 또한 관리자가 지정할 수 있어야 한다.
    - e.g. 필수 약관이 선택 약관보다 앞에 오도록 설정, 약관 내에서 특정 약관이 다른 약관보다 앞에 오도록 설정 등
    - 정렬 순서는 숫자가 작을수록 앞에 오도록 한다. (오름차순)
    - 정렬 순서가 동일한 경우에는 약관 ID 순으로 정렬한다.
- 새로운 약관이 추가되거나 기존 약관이 수정되었을 경우, 언제부터 적용될 것인지 활성 시작일을 지정해야 한다.
    - 활성 시작일이 현재 시점보다 미래인 경우, 해당 약관은 최신 버전이더라도 사용자에게 제공되지 않아야 한다.
- 약관은 상태(ACTIVE/INACTIVE)를 가져야 한다.


## 약관 데이터 모델링

하나의 약관에는 여러 버전이 존재할 수 있다.

### 약관 (TERMS)
```sql
CREATE TABLE terms
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    code          VARCHAR(30)  NOT NULL UNIQUE, # ('TERM_OF_SERVICE', 'TERM_OF_AGE', 'TERMS_OF_MARKETING', ...),
    title         VARCHAR(100) NOT NULL,
    type          VARCHAR(30)  NOT NULL,        # ('REQUIRED', 'OPTIONAL')
    display_order INT          NOT NULL,
    status        VARCHAR(10)  NOT NULL,        # ('ACTIVE', 'INACTIVE')
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_terms_code (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```

### 약관 버전 (TERM_VERSIONS)
```sql
CREATE TABLE term_versions
(
    term_version_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    term_id         BIGINT    NOT NULL,
    version         INT       NOT NULL,
    content         TEXT      NOT NULL,
    is_current      BOOLEAN   NOT NULL DEFAULT FALSE,
    effective_date  TIMESTAMP NULL, # 활성 시작일
    expiry_date     TIMESTAMP NULL, # 활성 종료일
    created_at      TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_term_version (term_id, version),
    INDEX idx_term_current (term_id, is_current),
    FOREIGN KEY (term_id) REFERENCES terms (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```