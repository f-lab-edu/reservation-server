use reservation;

DROP TABLE IF EXISTS term_versions;
DROP TABLE IF EXISTS terms;

CREATE TABLE terms
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    code          VARCHAR(30)  NOT NULL UNIQUE, # ('TERM_OF_SERVICE', 'TERM_OF_AGE', 'TERMS_OF_MARKETING', ...),
    title         VARCHAR(100) NOT NULL,
    type          VARCHAR(30)  NOT NULL,        # ('REQUIRED', 'OPTIONAL')
    display_order INT          NOT NULL,
    status         VARCHAR(10)  NOT NULL,        # ('ACTIVE', 'INACTIVE')
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_terms_code (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE term_versions
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    term_id             BIGINT    NOT NULL,
    version             INT       NOT NULL,
    content             TEXT      NOT NULL,
    is_current          BOOLEAN   NOT NULL DEFAULT FALSE,
    effective_date_time TIMESTAMP NULL, # 활성 시작일
    expiry_date_time    TIMESTAMP NULL, # 활성 종료일
    created_at          TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_term_version (term_id, version),
    INDEX idx_term_current (term_id, is_current),
    FOREIGN KEY (term_id) REFERENCES terms (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

desc term_versions;

DROP TABLE IF EXISTS phone_verifications;

CREATE TABLE phone_verifications
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone_number      VARCHAR(20) NOT NULL,
    verification_code VARCHAR(5)  NOT NULL,
    attempt_count     INT         NOT NULL DEFAULT 0, # 인증 시도 횟수 (최대 3회 제한)
    is_verified       BOOLEAN     NOT NULL DEFAULT FALSE,
    expired_at        TIMESTAMP   NOT NULL,
    verified_at       TIMESTAMP   NULL,
    created_at        TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_phone_number_code (phone_number, verification_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS user_term_agreements;
DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_password VARCHAR(60)  NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    nickname      VARCHAR(50)  NOT NULL,
    phone_number  VARCHAR(20)  NOT NULL UNIQUE,
    birth_date    DATE         NOT NULL,
    gender        VARCHAR(5)      NOT NULL, # ENUM ('M', 'F')
    role          VARCHAR(20)  NOT NULL, # ENUM ('USER', 'SUPPLIER', 'ADMIN')
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

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