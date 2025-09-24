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