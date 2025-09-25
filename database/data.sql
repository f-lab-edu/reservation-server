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

INSERT INTO terms (code, title, type, display_order, status)
VALUES ('TERM_SERVICE', '서비스 이용 약관', 'REQUIRED', 100, 'ACTIVE'),
       ('TERM_PRIVACY', '개인정보 처리방침', 'REQUIRED', 101, 'ACTIVE'),
       ('TERM_AGE', '만 14세 이상 확인 약관', 'REQUIRED', 102, 'ACTIVE'),
       ('TERM_MARKETING', '마케팅 정보 수신 동의 약관', 'OPTIONAL', 200, 'ACTIVE'),
       ('TERM_INFO', '개인정보 수집 및 이용 약관', 'OPTIONAL', 201, 'ACTIVE'),
       ('TERM_LOCATION', '위치 정보 이용 약관', 'OPTIONAL', 202, 'INACTIVE');

SELECT *
FROM terms;

desc term_versions;

INSERT INTO term_versions (term_id, version, content, is_current, effective_date_time)
VALUES (1, 1, '본 서비스 이용 동의..', false, NOW()),
       (1, 2, '본 서비스 이용 동의..', true, NOW()),
       (2, 1, '개인정보 처리방침에 따라..', false, NOW()),
       (2, 2, '개인정보 처리방침에 따라..', false, NOW()),
       (2, 3, '개인정보 처리방침에 따라..', true, NOW()),
       (3, 1, '본 만 14세 이상 확인 약관 동의..', true, NOW()),
       (4, 1, '본 마케팅 정보 수신 동의 약관 동의..', true, NOW()),
       (5, 1, '본 개인정보 수집 및 이용 약관 동의..', true, NOW()),
       (6, 1, '본 위치 정보 이용 약관 동의..', true, NOW());


EXPLAIN
select t.id, t.code, t.title, t.type, t.display_order, tv.version, tv.content
from term_versions tv
         join terms t on t.id = tv.term_id
where t.status = 'ACTIVE'
  and tv.is_current = 1
order by t.display_order