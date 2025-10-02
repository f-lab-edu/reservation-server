use reservation;

DROP TABLE IF EXISTS terms;

-- terms + term_versions 통합본
CREATE TABLE terms
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    code           VARCHAR(30)  NOT NULL,
    version        INT          NOT NULL,
    title          VARCHAR(100) NOT NULL,
    content        TEXT         NOT NULL,
    is_required    BOOLEAN      NOT NULL,
    display_order  INT          NOT NULL, # is_required = 0 ~ 500 , is_optional = 501 ~ 1000
    activated_at   DATETIME     NOT NULL, # 활성 시작일 (약관 적용 시점을 지정해야 함)
    deactivated_at DATETIME     NULL,     # 활성 종료일, 새로운 버전이 나오면 기존 버전에 종료일이 설정됨
    created_at     DATETIME DEFAULT NOW(),
    updated_at     DATETIME DEFAULT NOW() ON UPDATE NOW(),

    UNIQUE KEY uk_term_code_version (code, version),
    INDEX idx_term_code (code),

    CONSTRAINT chk_display_order_range
        CHECK (
            (is_required = TRUE AND display_order BETWEEN 0 AND 500) OR
            (is_required = FALSE AND display_order BETWEEN 501 AND 1000
                )
            )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

# 활성화된 약관 조회 쿼리 (활성화 시작일 <= 현재 시간 < 활성화 종료일(종료일이 NULL인 경우 무한대))
SELECT *
FROM terms
WHERE activated_at <= NOW()
  AND (deactivated_at IS NULL OR deactivated_at > NOW());

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
    created_at DATETIME DEFAULT NOW(),
    updated_at DATETIME DEFAULT NOW() ON UPDATE NOW()
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE user_term_agreements
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL,
    term_id         BIGINT NOT NULL,
    agreed_at DATETIME DEFAULT NOW(),

    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (term_id) REFERENCES terms (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

desc term_versions;

DROP TABLE IF EXISTS phone_verifications;

# 새로운 인증을 요청할 경우 기존 데이터를 덮어씌우는 방식으로 구현
CREATE TABLE phone_verifications
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone_number      VARCHAR(20) NOT NULL,
    verification_code VARCHAR(5)  NOT NULL,
    attempt_count     INT         NOT NULL DEFAULT 0, # 인증 시도 횟수 (최대 3회 제한)
    is_verified       BOOLEAN     NOT NULL DEFAULT FALSE,
    expired_at  DATETIME NOT NULL,
    verified_at DATETIME NULL,
    created_at  DATETIME DEFAULT NOW(),
    updated_at  DATETIME DEFAULT NOW() ON UPDATE NOW(),

    UNIQUE KEY uk_phone_number (phone_number)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;