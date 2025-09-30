use reservation;
DROP TABLE term_versions;
DROP TABLE terms;

desc terms;

#     TERM_SERVICE("서비스 이용약관"),
#     TERM_PRIVACY("개인정보 처리방침"),
#     TERM_MARKETING("마케팅 수신 동의"),
#     TERM_AGE("만 14세 이상 동의"),
#     TERM_INFO("개인정보 수집 및 이용 동의"),
#     TERM_LOCATION("위치 정보 동의"),

-- 통합된 terms 테이블에 데이터 삽입
INSERT INTO terms (code, version, title, content, is_required, display_order, activated_at, deactivated_at)
VALUES
('TERM_SERVICE', 1, '서비스 이용 약관', '본 서비스 이용 동의..', TRUE, 100, '2024-01-01 00:00:00', '2024-01-15 00:00:00'),
('TERM_SERVICE', 2, '서비스 이용 약관', '본 서비스 이용 동의..', TRUE, 100, '2024-01-15 00:00:00', NULL),
('TERM_PRIVACY', 1, '개인정보 처리방침', '개인정보 처리방침에 따라..', TRUE, 101, '2024-01-01 00:00:00', '2024-01-10 00:00:00'),
('TERM_PRIVACY', 2, '개인정보 처리방침', '개인정보 처리방침에 따라..', TRUE, 101, '2024-01-10 00:00:00', '2024-01-20 00:00:00'),
('TERM_PRIVACY', 3, '개인정보 처리방침', '개인정보 처리방침에 따라..', TRUE, 101, '2024-01-20 00:00:00', NULL),
('TERM_AGE', 1, '만 14세 이상 확인 약관', '본 만 14세 이상 확인 약관 동의..', TRUE, 102, '2024-01-01 00:00:00', NULL),

('TERM_MARKETING', 1, '마케팅 정보 수신 동의 약관', '본 마케팅 정보 수신 동의 약관 동의..', FALSE, 501, '2024-01-01 00:00:00', NULL),
('TERM_INFO', 1, '개인정보 수집 및 이용 약관', '본 개인정보 수집 및 이용 약관 동의..', FALSE, 502, '2024-01-01 00:00:00', NULL),
('TERM_LOCATION', 1, '위치 정보 이용 약관', '본 위치 정보 이용 약관 동의..', FALSE, 503, '2024-01-01 00:00:00', '2024-01-01 00:00:00');


EXPLAIN
select t.id, t.code, t.title, t.type, t.display_order, tv.version, tv.content
from term_versions tv
         join terms t on t.id = tv.term_id
where t.status = 'ACTIVE'
  and tv.is_current = 1
order by t.display_order