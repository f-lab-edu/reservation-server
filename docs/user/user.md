# 회원 (User)

## 요구사항

- 회원는 가입을 통해 서비스를 이용할 수 있어야 한다.
- 회원의 등급에 따라 접근 권한이 다르게 설정되어야 한다.
    - 일반 회원(User), 기업 회원(supplier), 관리자(admin) 역할이 존재한다.
    - 각 역할에 따라 접근할 수 있는 API가 다르다.
- 회원는 자신의 프로필 정보를 조회하고 수정할 수 있어야 한다.

## 회원 데이터 모델링

### 회원 (USERS)

```sql
CREATE TABLE users
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_password VARCHAR(60)  NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    nickname      VARCHAR(50)  NOT NULL,
    phone_number  VARCHAR(20)  NOT NULL UNIQUE,
    birth_date    DATE         NOT NULL,
    gender        CHAR(1)      NOT NULL, # ENUM ('M', 'F')
    role          VARCHAR(20)  NOT NULL, # ENUM ('USER', 'SUPPLIER', 'ADMIN')
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```

### 회원 약관 동의 목록 (user_term_agreements)

회원가입 시 사용자 약관 동의 목록 또한 저장해야 한다.

```sql
CREATE TABLE user_term_agreements
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL,
    term_id         BIGINT NOT NULL,
    term_version_id BIGINT NOT NULL,
    agreed_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (term_id) REFERENCES terms (id),
    FOREIGN KEY (term_version_id) REFERENCES term_versions (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```
