use reservation;

desc terms;

#     TERM_SERVICE("서비스 이용약관"),
#     TERM_PRIVACY("개인정보 처리방침"),
#     TERM_MARKETING("마케팅 수신 동의"),
#     TERM_AGE("만 14세 이상 동의"),
#     TERM_INFO("개인정보 수집 및 이용 동의"),
#     TERM_LOCATION("위치 정보 동의"),

-- 통합된 terms 테이블에 데이터 삽입
INSERT INTO terms (code, version, title, content, is_required, activated_at, deactivated_at)
VALUES ('TERM_SERVICE', 1, '서비스 이용 약관', '본 서비스 이용 동의..', TRUE, '2024-01-01 00:00:00', '2024-01-15 00:00:00'),
       ('TERM_SERVICE', 2, '서비스 이용 약관', '본 서비스 이용 동의..', TRUE, '2024-01-15 00:00:00', NULL),
       ('TERM_PRIVACY', 1, '개인정보 처리방침', '개인정보 처리방침에 따라..', TRUE, '2024-01-01 00:00:00', '2024-01-10 00:00:00'),
       ('TERM_PRIVACY', 2, '개인정보 처리방침', '개인정보 처리방침에 따라..', TRUE, '2024-01-10 00:00:00', '2024-01-20 00:00:00'),
       ('TERM_PRIVACY', 3, '개인정보 처리방침', '개인정보 처리방침에 따라..', TRUE, '2024-01-20 00:00:00', NULL),
       ('TERM_AGE', 1, '만 14세 이상 확인 약관', '본 만 14세 이상 확인 약관 동의..', TRUE, '2024-01-01 00:00:00', NULL),

       ('TERM_MARKETING', 1, '마케팅 정보 수신 동의 약관', '본 마케팅 정보 수신 동의 약관 동의..', FALSE, '2024-01-01 00:00:00', NULL),
       ('TERM_INFO', 1, '개인정보 수집 및 이용 약관', '본 개인정보 수집 및 이용 약관 동의..', FALSE, '2024-01-01 00:00:00', NULL),
       ('TERM_LOCATION', 1, '위치 정보 이용 약관', '본 위치 정보 이용 약관 동의..', FALSE, '2024-01-01 00:00:00', '2024-01-01 00:00:00');