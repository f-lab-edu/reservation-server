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
# JPA에서 @Where 옵션으로 활성화된 약관만 조회하도록 설정 가능
SELECT *
FROM terms
WHERE activated_at <= NOW()
  AND (deactivated_at IS NULL OR deactivated_at > NOW());