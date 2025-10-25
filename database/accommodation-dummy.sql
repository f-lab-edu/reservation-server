-- ================================================
-- 50만개 숙소 더미 데이터 생성 스크립트 (고품질, 다양성 중심)
-- ================================================
-- 실행 전 주의사항:
-- 1. supplier_id로 사용할 사용자가 존재해야 합니다.
-- 2. 데이터 생성에 약 5-10분 정도 소요됩니다.
-- 3. 최대한 중복을 피하고 다양한 데이터를 생성합니다.
-- ================================================

USE reservation;

-- 1. 테스트용 공급자 계정 생성 (없을 경우)
INSERT INTO users (user_password, email, nickname, phone_number, birth_date, gender, role)
VALUES
    ('$2a$10$dummy.hashed.password.for.testing.only', 'supplier1@test.com', '공급자1', '010-1000-0001', '1980-01-01', 'M', 'SUPPLIER'),
    ('$2a$10$dummy.hashed.password.for.testing.only', 'supplier2@test.com', '공급자2', '010-1000-0002', '1985-03-15', 'F', 'SUPPLIER'),
    ('$2a$10$dummy.hashed.password.for.testing.only', 'supplier3@test.com', '공급자3', '010-1000-0003', '1990-06-20', 'M', 'SUPPLIER'),
    ('$2a$10$dummy.hashed.password.for.testing.only', 'supplier4@test.com', '공급자4', '010-1000-0004', '1988-05-10', 'M', 'SUPPLIER'),
    ('$2a$10$dummy.hashed.password.for.testing.only', 'supplier5@test.com', '공급자5', '010-1000-0005', '1992-08-25', 'F', 'SUPPLIER')
ON DUPLICATE KEY UPDATE id=id;

-- 2. 프로시저 생성: 50만개 고품질 숙소 데이터 생성
DROP PROCEDURE IF EXISTS generate_accommodation_dummy_data;

DELIMITER //

CREATE PROCEDURE generate_accommodation_dummy_data()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE total_count INT DEFAULT 500000; -- 50만개로 변경
    DECLARE supplier_id_val BIGINT;
    DECLARE accommodation_name VARCHAR(100);
    DECLARE accommodation_desc TEXT;
    DECLARE accommodation_addr VARCHAR(255);
    DECLARE contact_num VARCHAR(20);
    DECLARE thumbnail_url VARCHAR(500);
    DECLARE status_val VARCHAR(30);
    DECLARE is_visible_val BOOLEAN;

    -- 한국 주요 지역 (90개 도시)
    DECLARE cities_arr VARCHAR(2000) DEFAULT '서울특별시,부산광역시,대구광역시,인천광역시,광주광역시,대전광역시,울산광역시,세종특별자치시,수원시,성남시,고양시,용인시,부천시,안산시,안양시,남양주시,화성시,평택시,의정부시,시흥시,파주시,김포시,광명시,광주시,군포시,이천시,양주시,오산시,구리시,안성시,포천시,의왕시,하남시,여주시,춘천시,원주시,강릉시,동해시,태백시,속초시,삼척시,청주시,충주시,제천시,천안시,공주시,보령시,아산시,서산시,논산시,계룡시,당진시,전주시,군산시,익산시,정읍시,남원시,김제시,목포시,여수시,순천시,나주시,광양시,포항시,경주시,김천시,안동시,구미시,영주시,영천시,상주시,문경시,경산시,창원시,진주시,통영시,사천시,김해시,밀양시,거제시,양산시,제주시,서귀포시';
    DECLARE city_name VARCHAR(30);
    DECLARE city_idx INT;

    -- 서울 구 세분화
    DECLARE seoul_districts VARCHAR(500) DEFAULT '강남구,서초구,송파구,강동구,마포구,종로구,중구,용산구,성동구,광진구,동대문구,중랑구,성북구,강북구,도봉구,노원구,은평구,서대문구,양천구,강서구,구로구,금천구,영등포구,동작구,관악구';
    DECLARE district_name VARCHAR(20);

    -- 숙소 타입 (15개)
    DECLARE types_arr VARCHAR(500) DEFAULT '호텔,모텔,펜션,리조트,게스트하우스,한옥스테이,풀빌라,캠핑장,글램핑,콘도,민박,비즈니스호텔,부티크호텔,호스텔,에어비앤비';
    DECLARE type_name VARCHAR(20);
    DECLARE type_idx INT;

    -- 랜덤 키워드 그룹 1: 뷰/전망 (20개)
    DECLARE view_keywords VARCHAR(500) DEFAULT '오션뷰,마운틴뷰,시티뷰,리버뷰,레이크뷰,스카이뷰,선셋뷰,포레스트뷰,파노라마뷰,루프탑뷰,발코니뷰,테라스뷰,가든뷰,파크뷰,비치프론트,워터프론트,힐탑,밸리뷰,하버뷰,베이뷰';

    -- 키워드 그룹 2: 특성/컨셉 (30개)
    DECLARE concept_keywords VARCHAR(800) DEFAULT '럭셔리,프리미엄,모던,클래식,빈티지,미니멀,스칸디나비안,인더스트리얼,보헤미안,로맨틱,힐링,휴양,감성,인스타감성,아늑한,넓은,깨끗한,조용한,프라이빗,독채,단독,신축,리모델링,디자이너스,아트,갤러리,뮤지엄,라이브러리,카페형,오피스텔형';

    -- 키워드 그룹 3: 타겟/용도 (20개)
    DECLARE target_keywords VARCHAR(500) DEFAULT '패밀리,커플,신혼부부,허니문,비즈니스,출장,워케이션,장기투숙,반려동물동반,반려견동반,키즈,영유아,베이비,시니어,청년,여행자,배낭여행,단체,세미나,파티룸';

    -- 키워드 그룹 4: 부대시설/서비스 (25개)
    DECLARE facility_keywords VARCHAR(600) DEFAULT '스파,온천,사우나,수영장,인피니티풀,자쿠지,헬스장,요가룸,BBQ,바비큐,캠프파이어,노래방,당구장,탁구장,보드게임,영화관,키즈카페,놀이터,무료조식,조식뷔페,미니바,주방,취사가능,세탁기,주차장';

    -- 브랜드/수식어 (40개)
    DECLARE brand_prefix VARCHAR(1000) DEFAULT '그랜드,파크,힐튼스타일,메리어트급,롯데,신라급,워커힐뷰,인터컨티넨탈급,하얏트감성,쉐라톤,웨스틴급,리젠트,플라자,타워,캐슬,팰리스,가든,베이,해비치,선샤인,골든,실버,로얄,임페리얼,프린스,퀸즈,킹스,듀크,마리나,하버,코스트,쇼어,비치,아일랜드,오아시스,파라다이스,에덴,샹그릴라,유토피아';

    -- 독특한 이름 접미사 (20개)
    DECLARE name_suffix VARCHAR(400) DEFAULT '스테이,하우스,레지던스,빌리지,가든,팰리스,스위트,시티,타운,파크,베이,힐,밸리,포레스트,플레이스,스퀘어,코너,랜드,월드,존';

    DECLARE keyword1 VARCHAR(30);
    DECLARE keyword2 VARCHAR(30);
    DECLARE keyword3 VARCHAR(30);
    DECLARE keyword4 VARCHAR(30);
    DECLARE brand_name VARCHAR(30);
    DECLARE suffix_name VARCHAR(20);
    DECLARE unique_number INT;

    DECLARE rand_num INT;

    -- 배치 처리를 위한 임시 비활성화
    SET autocommit = 0;

    SELECT CONCAT('50만개 고품질 데이터 생성 시작... (약 5-10분 소요)') AS info;

    WHILE i <= total_count DO
        -- 랜덤 supplier_id 선택
        SET supplier_id_val = (SELECT id FROM users WHERE role = 'SUPPLIER' ORDER BY RAND() LIMIT 1);

        -- 랜덤 지역 선택 (90개 도시 중 선택)
        SET city_idx = FLOOR(RAND() * 90) + 1;
        SET city_name = SUBSTRING_INDEX(SUBSTRING_INDEX(cities_arr, ',', city_idx), ',', -1);

        -- 서울인 경우 구까지 세분화
        IF city_name = '서울특별시' THEN
            SET district_name = SUBSTRING_INDEX(SUBSTRING_INDEX(seoul_districts, ',', FLOOR(RAND() * 25) + 1), ',', -1);
            SET city_name = CONCAT('서울 ', district_name);
        END IF;

        -- 랜덤 숙소 타입 선택
        SET type_idx = FLOOR(RAND() * 15) + 1;
        SET type_name = SUBSTRING_INDEX(SUBSTRING_INDEX(types_arr, ',', type_idx), ',', -1);

        -- 다양한 키워드 조합 선택 (각 그룹에서 하나씩)
        SET keyword1 = SUBSTRING_INDEX(SUBSTRING_INDEX(view_keywords, ',', FLOOR(RAND() * 20) + 1), ',', -1);
        SET keyword2 = SUBSTRING_INDEX(SUBSTRING_INDEX(concept_keywords, ',', FLOOR(RAND() * 30) + 1), ',', -1);
        SET keyword3 = SUBSTRING_INDEX(SUBSTRING_INDEX(target_keywords, ',', FLOOR(RAND() * 20) + 1), ',', -1);
        SET keyword4 = SUBSTRING_INDEX(SUBSTRING_INDEX(facility_keywords, ',', FLOOR(RAND() * 25) + 1), ',', -1);

        -- 브랜드명 (40% 확률로 추가)
        IF RAND() < 0.4 THEN
            SET brand_name = SUBSTRING_INDEX(SUBSTRING_INDEX(brand_prefix, ',', FLOOR(RAND() * 40) + 1), ',', -1);
        ELSE
            SET brand_name = '';
        END IF;

        -- 접미사 (30% 확률로 추가)
        IF RAND() < 0.3 THEN
            SET suffix_name = SUBSTRING_INDEX(SUBSTRING_INDEX(name_suffix, ',', FLOOR(RAND() * 20) + 1), ',', -1);
        ELSE
            SET suffix_name = '';
        END IF;

        -- 고유 번호 (i를 포함하여 중복 최소화)
        SET unique_number = i;

        -- 숙소명 생성 (12가지 패턴으로 다양성 극대화)
        CASE FLOOR(RAND() * 12)
            WHEN 0 THEN
                -- "브랜드 + 도시 + 타입 + 번호"
                IF brand_name != '' THEN
                    SET accommodation_name = CONCAT(brand_name, ' ', city_name, ' ', type_name, ' ', unique_number, '호');
                ELSE
                    SET accommodation_name = CONCAT(city_name, ' ', keyword2, ' ', type_name, ' ', unique_number, '호');
                END IF;
            WHEN 1 THEN
                -- "도시 + 키워드1 + 타입 + 번호"
                SET accommodation_name = CONCAT(city_name, ' ', keyword1, ' ', type_name, ' ', unique_number);
            WHEN 2 THEN
                -- "키워드2 + 키워드1 + 접미사 + 번호"
                IF suffix_name != '' THEN
                    SET accommodation_name = CONCAT(keyword2, ' ', keyword1, ' ', suffix_name, ' ', unique_number);
                ELSE
                    SET accommodation_name = CONCAT(keyword2, ' ', city_name, ' ', type_name, ' ', unique_number);
                END IF;
            WHEN 3 THEN
                -- "[도시] 브랜드 + 키워드2 + 타입 + 번호"
                SET accommodation_name = CONCAT('[', city_name, '] ', brand_name, ' ', keyword2, ' ', type_name, ' ', unique_number);
            WHEN 4 THEN
                -- "도시 + 키워드3 전용 + 타입 + 번호"
                SET accommodation_name = CONCAT(city_name, ' ', keyword3, '전용 ', type_name, ' ', unique_number);
            WHEN 5 THEN
                -- "키워드4 + 도시 + 접미사 + 번호"
                IF suffix_name != '' THEN
                    SET accommodation_name = CONCAT(keyword4, ' ', city_name, ' ', suffix_name, ' ', unique_number);
                ELSE
                    SET accommodation_name = CONCAT(keyword4, ' ', city_name, ' ', type_name, ' ', unique_number);
                END IF;
            WHEN 6 THEN
                -- "브랜드 + 키워드1 + 키워드2 + 타입 + 번호"
                SET accommodation_name = CONCAT(brand_name, ' ', keyword1, ' ', keyword2, ' ', type_name, ' ', unique_number);
            WHEN 7 THEN
                -- "도시 + 타입 + with + 키워드4 + 번호"
                SET accommodation_name = CONCAT(city_name, ' ', type_name, ' with ', keyword4, ' ', unique_number);
            WHEN 8 THEN
                -- "더 + 키워드2 + city_name + 접미사 + 번호"
                SET accommodation_name = CONCAT('더 ', keyword2, ' ', city_name, ' ', suffix_name, ' ', unique_number);
            WHEN 9 THEN
                -- "키워드3 + 를 위한 + 도시 + 타입 + 번호"
                SET accommodation_name = CONCAT(keyword3, '를 위한 ', city_name, ' ', type_name, ' ', unique_number);
            WHEN 10 THEN
                -- "브랜드 + 도시 + 키워드1 + 접미사 + 번호"
                SET accommodation_name = CONCAT(brand_name, ' ', city_name, ' ', keyword1, ' ', suffix_name, ' ', unique_number);
            ELSE
                -- "감성 조합 + 번호"
                SET accommodation_name = CONCAT(city_name, ' ', keyword1, ' ', keyword2, ' ', keyword3, ' ', type_name, ' ', unique_number);
        END CASE;

        -- 설명 생성 (구체적이고 다양하게)
        SET accommodation_desc = CONCAT(
            city_name, '에 위치한 ', keyword2, ' 스타일의 ', type_name, '입니다. ',
            keyword1, ' 전망을 자랑하며, ', keyword3, ' 고객님들께 최적화된 공간입니다. ',

            -- 시설 정보 (랜덤하게 2-3개 조합)
            IF(RAND() < 0.4, CONCAT(keyword4, ' 시설을 갖추고 있으며, '), ''),
            IF(RAND() < 0.3, '전 객실 오션뷰로 구성되어 있고, ', ''),
            IF(RAND() < 0.3, '프라이빗한 휴식 공간을 제공하며, ', ''),

            -- 서비스 정보
            IF(RAND() < 0.5, '24시간 컨시어지 서비스, ', ''),
            IF(RAND() < 0.4, '무료 공항 셔틀, ', ''),
            IF(RAND() < 0.35, '웰컴 드링크 제공, ', ''),

            -- 특징 정보
            IF(RAND() < 0.3, '전 객실 스마트 홈 시스템, ', ''),
            IF(RAND() < 0.25, '인스타그램 감성 인테리어, ', ''),
            IF(RAND() < 0.3, '친환경 어메니티 제공, ', ''),

            -- 부가 정보
            IF(RAND() < 0.5, CONCAT('주변에 ', ELT(FLOOR(RAND() * 5) + 1, '유명 맛집', '쇼핑몰', '관광 명소', '해변', '산책로'), '가 있습니다. '), ''),
            IF(RAND() < 0.4, CONCAT(ELT(FLOOR(RAND() * 4) + 1, '역에서 도보 5분', '공항에서 차로 20분', '시내 중심가 인접', '자연 속 힐링 공간'), '. '), ''),

            -- 추가 서비스
            IF(RAND() < 0.3, '반려동물 동반 가능. ', ''),
            IF(RAND() < 0.4, '장기 투숙 할인 제공. ', ''),
            IF(RAND() < 0.3, '조식 뷔페 무료 제공. ', ''),
            IF(RAND() < 0.25, '전용 주차장 완비. ', ''),
            IF(RAND() < 0.2, '루프탑 바 운영. ', ''),

            '편안하고 특별한 경험을 선사합니다.'
        );

        -- 주소 생성 (더 실제감 있게, 중복 최소화)
        SET accommodation_addr = CONCAT(
            city_name, ' ',
            CASE
                WHEN city_name LIKE '%서울%' THEN
                    ELT(FLOOR(RAND() * 15) + 1, '테헤란로', '강남대로', '논현로', '봉은사로', '선릉로', '역삼로', '언주로', '삼성로', '영동대로', '도산대로', '압구정로', '청담로', '가로수길', '압구정로', '선정릉로')
                WHEN city_name LIKE '%부산%' THEN
                    ELT(FLOOR(RAND() * 8) + 1, '해운대해변로', '광안해변로', '중앙대로', '수영로', '센텀중앙로', '달맞이길', '동백로', '해운대로')
                WHEN city_name LIKE '%제주%' THEN
                    ELT(FLOOR(RAND() * 8) + 1, '중앙로', '일주동로', '애월해안로', '한림로', '성산읍', '표선면', '조천읍', '한경면')
                WHEN city_name LIKE '%강릉%' OR city_name LIKE '%속초%' THEN
                    ELT(FLOOR(RAND() * 5) + 1, '해변로', '해안로', '중앙로', '항구로', '관광로')
                ELSE
                    ELT(FLOOR(RAND() * 10) + 1, '중앙로', '시청로', '역전로', '공원로', '문화로', '평화로', '번영로', '행복로', '희망로', '발전로')
            END,
            ' ', FLOOR(RAND() * 500 + 1),
            IF(RAND() < 0.4, CONCAT('번길 ', FLOOR(RAND() * 100 + 1)), ''),
            IF(RAND() < 0.2, CONCAT(', ', FLOOR(RAND() * 30 + 1), '층'), '') -- 층 정보 추가
        );

        -- 연락처 생성 (지역별 실제 번호 형식)
        SET contact_num = CONCAT(
            CASE
                WHEN city_name LIKE '%서울%' THEN '02'
                WHEN city_name LIKE '%부산%' THEN '051'
                WHEN city_name LIKE '%대구%' THEN '053'
                WHEN city_name LIKE '%인천%' THEN '032'
                WHEN city_name LIKE '%광주%' THEN '062'
                WHEN city_name LIKE '%대전%' THEN '042'
                WHEN city_name LIKE '%울산%' THEN '052'
                WHEN city_name LIKE '%경기%' OR city_name LIKE '%수원%' OR city_name LIKE '%성남%' OR city_name LIKE '%고양%' THEN '031'
                WHEN city_name LIKE '%강원%' OR city_name LIKE '%춘천%' OR city_name LIKE '%강릉%' THEN '033'
                WHEN city_name LIKE '%충북%' OR city_name LIKE '%청주%' THEN '043'
                WHEN city_name LIKE '%충남%' OR city_name LIKE '%천안%' THEN '041'
                WHEN city_name LIKE '%전북%' OR city_name LIKE '%전주%' THEN '063'
                WHEN city_name LIKE '%전남%' OR city_name LIKE '%목포%' THEN '061'
                WHEN city_name LIKE '%경북%' OR city_name LIKE '%포항%' THEN '054'
                WHEN city_name LIKE '%경남%' OR city_name LIKE '%창원%' THEN '055'
                WHEN city_name LIKE '%제주%' THEN '064'
                ELSE '02'
            END,
            '-', LPAD(FLOOR(RAND() * 9000 + 1000), 4, '0'),
            '-', LPAD(FLOOR(RAND() * 9000 + 1000), 4, '0')
        );

        -- 상태 (90%는 APPROVED로 검색 데이터 확보)
        SET rand_num = FLOOR(RAND() * 100);
        SET status_val = CASE
            WHEN rand_num < 90 THEN 'APPROVED'
            WHEN rand_num < 94 THEN 'PENDING'
            WHEN rand_num < 98 THEN 'REJECTED'
            ELSE 'SUSPENDED'
        END;

        -- 노출 여부 (APPROVED인 경우 95%는 노출)
        SET is_visible_val = CASE
            WHEN status_val = 'APPROVED' AND RAND() < 0.95 THEN TRUE
            ELSE FALSE
        END;

        -- 썸네일 URL 생성 (http://test.com/{숙소번호} 형식)
        SET thumbnail_url = CONCAT('http://test.com/', i);

        -- 데이터 삽입
        INSERT INTO accommodations (supplier_id, name, description, address, contact_number, thumbnail, status, is_visible, created_at)
        VALUES (
            supplier_id_val,
            accommodation_name,
            accommodation_desc,
            accommodation_addr,
            contact_num,
            thumbnail_url,
            status_val,
            is_visible_val,
            DATE_ADD(NOW(), INTERVAL -FLOOR(RAND() * 730) DAY)  -- 최근 2년 내 랜덤 생성일
        );

        SET i = i + 1;

        -- 10000개마다 커밋 및 진행률 출력
        IF i MOD 10000 = 0 THEN
            COMMIT;
            SELECT CONCAT('진행률: ', i, '/', total_count, ' (', ROUND(i/total_count*100, 2), '%)') AS progress;
        END IF;

    END WHILE;

    COMMIT;
    SET autocommit = 1;

    SELECT CONCAT('✅ 완료! 총 ', total_count, '개의 고품질 숙소 데이터가 생성되었습니다.') AS result;

END //

DELIMITER ;

-- 3. 프로시저 실행
CALL generate_accommodation_dummy_data();