create database if not exists reservation;

use reservation;

DROP TABLE IF EXISTS room_units;
DROP TABLE IF EXISTS room_types;
DROP TABLE IF EXISTS accommodation_status_histories;
DROP TABLE IF EXISTS accommodations;
DROP TABLE IF EXISTS user_term_agreements;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS terms;
DROP TABLE IF EXISTS phone_verifications;

-- 약관 테이블 (복합키 기반 버전 관리)
CREATE TABLE terms
(
    code           VARCHAR(30)  NOT NULL,
    version        INT          NOT NULL,
    title          VARCHAR(100) NOT NULL,
    content        TEXT         NOT NULL,
    is_required    BOOLEAN      NOT NULL,
    activated_at   DATETIME     NOT NULL, # 활성 시작일 (약관 적용 시점을 지정해야 함)
    deactivated_at DATETIME     NULL,     # 활성 종료일, 새로운 버전이 나오면 기존 버전에 종료일이 설정됨
    created_at     DATETIME DEFAULT NOW(),
    updated_at     DATETIME DEFAULT NOW() ON UPDATE NOW(),

    PRIMARY KEY (code, version)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 회원 테이블
CREATE TABLE users
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_password VARCHAR(60)  NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    nickname      VARCHAR(50)  NOT NULL,
    phone_number  VARCHAR(20)  NOT NULL UNIQUE,
    birth_date    DATE         NOT NULL,
    gender        VARCHAR(5)   NOT NULL, # ENUM ('M', 'F')
    role          VARCHAR(20)  NOT NULL, # ENUM ('USER', 'SUPPLIER', 'ADMIN')
    created_at    DATETIME DEFAULT NOW(),
    updated_at    DATETIME DEFAULT NOW() ON UPDATE NOW()
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 회원 약관 동의 이력 테이블
CREATE TABLE user_term_agreements
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id   BIGINT NOT NULL,
    term_code    VARCHAR(30) NOT NULL,
    term_version INT         NOT NULL,
    agreed_at DATETIME DEFAULT NOW(),

    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (term_code, term_version) REFERENCES terms (code, version)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;



-- 휴대폰 인증 테이블 (새로운 인증을 요청할 경우 기존 데이터를 덮어씌우는 방식으로 구현)
CREATE TABLE phone_verifications
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone_number      VARCHAR(20) NOT NULL,
    verification_code VARCHAR(5)  NOT NULL,
    attempt_count     INT         NOT NULL DEFAULT 0, # 인증 시도 횟수 (최대 3회 제한)
    is_verified       BOOLEAN     NOT NULL DEFAULT FALSE,
    expired_at        DATETIME    NOT NULL,
    verified_at       DATETIME    NULL,
    last_sent_at      DATETIME    NOT NULL,
    created_at        DATETIME             DEFAULT NOW(),
    updated_at        DATETIME             DEFAULT NOW() ON UPDATE NOW(),

    UNIQUE KEY uk_phone_number (phone_number)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- 숙소 테이블
CREATE TABLE accommodations
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_id    BIGINT       NOT NULL, # 숙소 등록한 공급자 (users 테이블의 id 참조)
    name           VARCHAR(100) NOT NULL,
    description    TEXT         NOT NULL,
    address        VARCHAR(255) NOT NULL, # 전체 주소 (저장 방식 어떻게 할지 고민해야 함)
    contact_number VARCHAR(20)  NOT NULL,
    thumbnail      VARCHAR(500) NOT NULL, # 썸네일 이미지 URL
    status         VARCHAR(30)  NOT NULL, # ENUM('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')
    is_visible     BOOLEAN      NOT NULL, # 숙소 노출 여부
    created_at     DATETIME DEFAULT NOW(),
    updated_at     DATETIME DEFAULT NOW() ON UPDATE NOW(),

    FULLTEXT INDEX ft_idx_name_address (name, address) WITH PARSER ngram,
    FOREIGN KEY (supplier_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 숙소 상태 변경 이력 테이블
CREATE TABLE accommodation_status_histories
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    accommodation_id BIGINT      NOT NULL,
    previous_status  VARCHAR(30) NOT NULL,
    new_status       VARCHAR(30) NOT NULL,
    reason           TEXT        NULL,     # 상태 변경 사유 (공급자 또는 관리자 입력)
    changed_by       BIGINT      NOT NULL, # 상태 변경한 사용자 (users 테이블의 id 참조)
    changed_at       DATETIME DEFAULT NOW(),

    FOREIGN KEY (accommodation_id) REFERENCES accommodations (id),
    FOREIGN KEY (changed_by) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 객실 타입 테이블
CREATE TABLE room_types
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    accommodation_id  BIGINT         NOT NULL,
    name              VARCHAR(100)   NOT NULL,
    description       TEXT           NOT NULL,
    standard_capacity INT            NOT NULL, # 표준 인원 수
    max_capacity      INT            NOT NULL, # 최대 인원 수
    base_price        DECIMAL(10, 2) NOT NULL, # 기본 요금
    total_room_count  INT            NOT NULL,
    thumbnail         VARCHAR(255)   NOT NULL, # 썸네일 이미지 URL
    created_at        DATETIME DEFAULT NOW(),
    updated_at        DATETIME DEFAULT NOW() ON UPDATE NOW(),
    FOREIGN KEY (accommodation_id) REFERENCES accommodations (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 객실 유닛 테이블
CREATE TABLE room_units
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type_id BIGINT       NOT NULL,
    room_number  VARCHAR(20)  NOT NULL,
    status       VARCHAR(30)  NOT NULL,
    notes        VARCHAR(255) NULL, # 객실 유닛에 대한 추가 정보
    created_at   DATETIME DEFAULT NOW(),
    updated_at   DATETIME DEFAULT NOW() ON UPDATE NOW(),

    FOREIGN KEY (room_type_id) REFERENCES room_types (id),
    UNIQUE KEY uk_room_type_id_room_number (room_type_id, room_number)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- 예약 테이블 (검색 기능 구현용)
-- 고객이 객실 타입(room_type)을 선택하여 예약 생성
-- 체크인 시점에 공급자가 실제 객실(room_unit)을 배정
-- TODO: 실제 예약 시스템 구현 시 확장 필요 (사용자 정보, 결제 정보 등)
CREATE TABLE reservations
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type_id BIGINT NOT NULL, # room_types 테이블의 id 참조 (예약은 타입 단위로)
    check_in     DATE   NOT NULL,
    check_out    DATE   NOT NULL,
    created_at   DATETIME DEFAULT NOW(),
    updated_at   DATETIME DEFAULT NOW() ON UPDATE NOW(),

    INDEX idx_room_type_dates (room_type_id, check_in, check_out),
    FOREIGN KEY (room_type_id) REFERENCES room_types (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 예약 확정 테이블 (향후 구현 예정)
-- 결제 완료 후 예약이 확정되면 실제 객실(room_unit)을 배정하는 테이블
-- CREATE TABLE reservation_confirmations
-- (
--     id              BIGINT PRIMARY KEY AUTO_INCREMENT,
--     reservation_id  BIGINT      NOT NULL,  # reservations 테이블의 id
--     room_unit_id    BIGINT      NOT NULL,  # 실제 배정된 객실 (room_units 테이블의 id)
--     user_id         BIGINT      NOT NULL,  # 예약자
--     status          VARCHAR(30) NOT NULL,  # ENUM('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED')
--     payment_id      BIGINT      NULL,      # 결제 정보
--     confirmed_at    DATETIME    NOT NULL,  # 예약 확정 시각
--     created_at      DATETIME DEFAULT NOW(),
--     updated_at      DATETIME DEFAULT NOW() ON UPDATE NOW(),
--
-- ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

