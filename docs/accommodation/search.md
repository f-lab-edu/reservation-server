# MySQL 검색 성능 최적화: LIKE → FULLTEXT INDEX

## 요약
- 기존 `LIKE '%키워드%'` 검색은 선행 와일드카드로 인해 전수 스캔이 발생해 1초 이상 지연되었다.
- MySQL 8.4 Ngram FULLTEXT 인덱스를 도입해 평균 0.01초 수준으로 응답 시간을 단축했다.
- Stop-word Parser와 Ngram Parser의 적용 조건, 인덱스 크기, 성능을 비교해 한국어 환경에서는 Ngram이 적합하다는 결론을 얻었다.
- 성능 측정을 위해 EXPLAIN ANALYZE로 4가지 시나리오를 테스트하고, LIMIT/정렬 전략까지 점검했다.

## 1. 문제 인식: LIKE 연산의 한계

### 1.1 초기 상황

기존 숙소 검색 시스템은 LIKE 연산을 사용하여 키워드 검색을 구현했습니다. 사용자가 "속초 오션뷰"를 검색하면 다음 쿼리가 실행되었습니다.

```sql
SELECT *
FROM accommodations
WHERE name LIKE '%속초 오션뷰%'
   OR address LIKE '%속초 오션뷰%'
   OR description LIKE '%속초 오션뷰%';
```

### 1.2 성능 문제

1. **Full Table Scan 발생**: 선행 와일드카드(`%`)로 인해 인덱스를 사용할 수 없어, 전체 테이블을 순차 스캔합니다.
2. **다중 컬럼 검색의 비효율**: 약 50만 행 × 3개 컬럼 = 153만 번의 패턴 매칭이 필요합니다.
3. **정확한 문자열만 매칭**: 띄어쓰기나 단어 순서가 조금만 달라도 검색되지 않습니다.
    - "속초시 오션뷰" 검색 불가
    - "오션뷰 속초" 검색 불가
    - "속초 바다뷰" 검색 불가

### 1.3 성능 측정 결과

| 검색어       | 실행 시간 | 결과 수   | 문제점             |
|-----------|-------|--------|-----------------|
| "속초 오션뷰"  | 1.07초 | 0개     | 정확한 문자열 없음      |
| "속초시 오션뷰" | 1.06초 | 88개    | 표기 변형 누락        |
| "하얏트"     | 1.27초 | 1,848개 | Full Table Scan |

## 2. 해결 방안: FULLTEXT INDEX 도입

> https://dev.mysql.com/doc/refman/8.4/en/fulltext-search.html

### 2.1 FULLTEXT INDEX란?

FULLTEXT INDEX는 텍스트 검색에 특화된 인덱스로, **키워드를 토큰으로 분해하여 인덱싱**합니다.

### 2.2 인덱스 생성

```sql
-- Ngram Parser를 사용한 FULLTEXT INDEX 생성
CREATE FULLTEXT INDEX ft_idx
    ON accommodations (name, address, description)
    WITH PARSER ngram;
```

### 2.3 검색 쿼리 변경

```sql
-- Natural Language Mode (OR 검색, 관련성 점수 기반)
SELECT *
FROM accommodations
WHERE MATCH(name, address, description) AGAINST('속초 오션뷰')
LIMIT 20;

-- Boolean Mode (AND 검색, 정확한 매칭)
SELECT *
FROM accommodations
WHERE MATCH(name, address, description) AGAINST('+속초 +오션뷰' IN BOOLEAN MODE)
LIMIT 20;
```

## 3. FULLTEXT INDEX 파서 비교

### 3.1 Stop-word Parser (기본 파서)

**동작 원리**: 공백과 특수문자를 기준으로 단어를 분리합니다.

```
"속초 오션뷰 풀빌라" → ["속초", "오션뷰", "풀빌라"]
```

**최소 토큰 길이 제약**: `innodb_ft_min_token_size` 기본값은 3입니다.

```sql
SHOW VARIABLES LIKE 'innodb_ft_min_token_size';
-- 결과: 3
```

이는 3글자 미만의 단어는 인덱싱되지 않음을 의미합니다.

- "속초" (2글자): 인덱싱 안 됨
- "오션뷰" (3글자): 인덱싱 됨
- "풀빌라" (3글자): 인덱싱 됨

**장점**

- 인덱스 크기가 상대적으로 작음
- 의미 단위 검색 가능
- 빠른 검색 속도 (적절한 결과 수일 때)

**단점**

- 한글 2글자 검색 불가 ("서울", "부산", "제주")
- 띄어쓰기에 의존적
- "속초풀빌라" (붙여쓰기)는 하나의 토큰으로 인식

### 3.2 Ngram Parser (MySQL 5.7+)

**동작 원리**: 글자 단위로 n개씩 분해하여 토큰을 생성합니다.

```
# 기본값 `ngram_token_size=2` (2-gram)

"속초오션뷰" → ["속초", "초오", "오션", "션뷰"]
```

_인덱스 생성(MySQL)_

```sql
CREATE FULLTEXT INDEX ft_idx_ngram
    ON accommodations (name, address, description)
    WITH PARSER ngram;
```

**장점**

- 2글자 검색 가능: "서울", "부산", "제주" 모두 검색 가능
- 띄어쓰기 무관: "속초풀빌라", "속초 풀빌라" 모두 매칭
- 부분 문자열 검색: "풀빌라"로 "속초 풀빌라" 검색 가능

**단점**

- 인덱스 크기 폭증: Stop-word 대비 3~5배 큰 인덱스
- Boolean Mode에서 느림: Stop-word 대비 2~3배 느림

## 4. 성능 테스트 설계

### 4.1 테스트 환경

**데이터셋**: 500,000개의 숙소 데이터

**테스트 컬럼**

- name (숙소명)
- address (주소)
- description (설명)

**인덱스 구성**

- 기존: 인덱스 없음 (LIKE 연산만 사용)
- 개선: FULLTEXT INDEX (Ngram Parser 적용)

**측정 도구**: MySQL `EXPLAIN ANALYZE` 명령어 사용

### 4.2 테스트 시나리오

**시나리오 1**: 복합 키워드 검색 + LIMIT (실제 사용 케이스)

- 검색어: "속초 오션뷰"
- 조건: LIMIT 30 적용
- 목적: 페이지네이션 환경에서의 성능 측정

**시나리오 2**: 고유 브랜드명 검색 (소수 결과)

- 검색어: "하얏트"
- 조건: LIMIT 없음
- 목적: 결과 비율 0.1~1% 구간 성능 측정

**시나리오 3**: 흔한 키워드 검색 (다량 결과)

- 검색어: "풀빌라"
- 조건: LIMIT 없음
- 목적: 결과 비율 5% 이상 구간 성능 측정

**시나리오 4**: 정확한 문구 검색

- 검색어: "속초시 오션뷰"
- 조건: LIMIT 없음
- 목적: 정확한 문자열 매칭 성능 비교

### 4.3 테스트 쿼리

#### LIKE 연산 테스트

```sql
EXPLAIN ANALYZE
SELECT *
FROM accommodations
WHERE name LIKE '%속초 오션뷰%'
   OR address LIKE '%속초 오션뷰%'
   OR description LIKE '%속초 오션뷰%'
LIMIT 30;
```

#### FULLTEXT Natural Language Mode 테스트

```sql
EXPLAIN ANALYZE
SELECT *
FROM accommodations
WHERE MATCH(name, address, description) AGAINST('속초 오션뷰')
LIMIT 30;
```

#### FULLTEXT Boolean Mode 테스트

```sql
EXPLAIN ANALYZE
SELECT *
FROM accommodations
WHERE MATCH(name, address, description) AGAINST('+속초 +오션뷰' IN BOOLEAN MODE)
LIMIT 30;
```

## 5. 성능 테스트 결과

### 5.1 시나리오 1: 복합 키워드 + LIMIT (핵심 개선 사례)

**검색어**: "속초 오션뷰" (LIMIT 30)

| 방식               | 실행 시간   | 스캔 행 수  | 결과 수 | 성능 향상      |
|------------------|---------|---------|------|------------|
| LIKE OR          | 1,232ms | 510,000 | 0개   | 기준         |
| FULLTEXT Natural | 37.8ms  | 30      | 30개  | **28배 빠름** |
| FULLTEXT Boolean | 40.4ms  | 30      | 30개  | **27배 빠름** |

**EXPLAIN ANALYZE 상세 결과**:

```sql
-- LIKE 결과
-> Limit: 30 row(s)  (cost=56505 rows=30) (actual time=1232..1232 rows=0 loops=1)
    -> Filter: ((accommodations.`name` like '%속초 오션뷰%') or (accommodations.address like '%속초 오션뷰%') or (accommodations.`description` like '%속초 오션뷰%'))  (cost=56505 rows=131957) (actual time=1232..1232 rows=0 loops=1)
        -> Table scan on accommodations  (cost=56505 rows=443341) (actual time=0.338..724 rows=500000 loops=1)

-- FULLTEXT Natural Language 결과
-> Limit: 30 row(s)  (cost=0.858 rows=1) (actual time=37.4..37.8 rows=30 loops=1)
    -> Filter: (match against ('속초 오션뷰'))  
       (cost=0.858 rows=1) (actual time=37.4..37.8 rows=30 loops=1)
        -> Full-text index search using ft_idx  
           (cost=0.858 rows=1) (actual time=37.4..37.7 rows=30 loops=1)
```

**분석**:

- LIKE는 50만 행 전체를 스캔했지만 0개 발견 (정확한 문자열 없음)
- FULLTEXT는 인덱스에서 직접 30개를 찾고 즉시 종료
- Natural Language Mode가 Boolean Mode보다 약간 빠름 (OR 검색 최적화)

### 5.2 시나리오 2: 고유 브랜드명 검색 (최고 성능)

**검색어**: "하얏트" (결과 1,848개 = 0.36%)

| 방식               | 실행 시간    | 스캔 행 수  | 결과 수   | 성능 향상       |
|------------------|----------|---------|--------|-------------|
| LIKE OR          | 1,270ms  | 510,000 | 1,848개 | 기준          |
| FULLTEXT Natural | **10ms** | 1,848   | 1,848개 | **127배 빠름** |
| FULLTEXT Boolean | 50ms     | 1,848   | 1,848개 | **25배 빠름**  |

**EXPLAIN ANALYZE 상세 결과**:

```sql
-- LIKE 결과
-> Filter: ((name like '%하얏트%') or (address like '%하얏트%') 
    or (description like '%하얏트%'))  
   (cost=57055 rows=131957) (actual time=0.611..1214 rows=1848 loops=1)
    -> Table scan on accommodations  
       (cost=57055 rows=443341) (actual time=0.482..696 rows=500000 loops=1)

-- FULLTEXT Natural Language 결과
-> Filter: (match against ('하얏트'))  
   (cost=0.837 rows=1) (actual time=0.684..22.5 rows=1848 loops=1)
    -> Full-text index search using ft_idx  
       (cost=0.837 rows=1) (actual time=0.657..21.8 rows=1848 loops=1)
```

**분석**:

- 결과 비율 0.36%는 FULLTEXT의 최적 구간
- LIKE는 51만 행을 모두 스캔 후 1,848개 반환
- FULLTEXT는 인덱스에서 1,848개만 직접 조회
- 실제 쿼리 실행 시간은 0.01초로 EXPLAIN ANALYZE보다 빠름 (프로파일링 오버헤드 제외)

### 5.3 시나리오 3: 흔한 키워드 검색 (FULLTEXT 한계)

**검색어**: "풀빌라" (결과 26,710개 = 5.2%)

| 방식               | 실행 시간     | 스캔 행 수  | 결과 수    | 비고          |
|------------------|-----------|---------|---------|-------------|
| LIKE OR          | **470ms** | 510,000 | 26,710개 | **LIKE 승리** |
| FULLTEXT Natural | 800ms     | 26,710  | 26,710개 | LIKE보다 느림   |
| FULLTEXT Boolean | 550ms     | 26,710  | 26,710개 | LIKE보다 느림   |

**분석**:

- 결과가 5% 이상일 때는 FULLTEXT의 인덱스 오버헤드가 더 큼
- 26,710개의 Random I/O(인덱스→테이블 룩업)가 Sequential Scan보다 느림
- LIMIT 없이 전체 결과를 가져오는 경우 LIKE가 유리

### 5.4 시나리오 4: 정확한 문구 검색

**검색어**: "속초시 오션뷰" (OR 검색 시 174,725개 = 34%)

| 방식               | 실행 시간       | 결과 수     | 비고         |
|------------------|-------------|----------|------------|
| LIKE OR          | **1,060ms** | 88개      | 정확한 문구만 매칭 |
| FULLTEXT Natural | 1,780ms     | 174,725개 | 과다 매칭      |
| FULLTEXT Boolean | 3,170ms     | 174,725개 | 과다 매칭      |

**분석**:

- LIKE는 정확한 문구 "속초시 오션뷰"만 찾아 88개 반환
- FULLTEXT는 "속초시" 또는 "오션뷰" 중 하나라도 포함하면 매칭 (OR 검색)
- 결과가 34%로 너무 많아 FULLTEXT가 비효율적
- 이 경우 AND 조건(`'+속초시 +오션뷰'`)으로 변경 필요

## 6. 성능 비교 종합

| 결과 비율  | 추천 방식            | 이유                    |
|--------|------------------|-----------------------|
| 0.1~1% | FULLTEXT         | 10~100배 빠름, 인덱스 효율 최고 |
| 1~5%   | FULLTEXT + LIMIT | LIMIT 사용 시 여전히 유리     |
| 5% 이상  | LIKE 또는 필터 추가    | FULLTEXT 인덱스 오버헤드 증가  |

### 6.3 Natural Language vs Boolean Mode

**Natural Language Mode 추천 상황**:

- OR 검색 (키워드 중 하나라도 포함)
- 관련성 점수 기반 정렬 필요
- 단일 키워드 검색

**Boolean Mode 추천 상황**:

- AND 검색 (모든 키워드 필수)
- 정확한 매칭 필요
- 복합 조건 검색 (+, -, * 연산자 사용)

## 7. 실전 적용 가이드

### 7.1 언제 LIKE를 사용할까?

**사용 케이스**:

- 정확한 문자열 패턴 검색 (이메일, 전화번호)
- 데이터가 1만 건 미만
- 검색 빈도가 낮음
- 정규표현식 패턴 매칭 필요

### 7.2 언제 Stop-word FULLTEXT를 사용할까?

**사용 케이스**:

- 고유 키워드 검색 (브랜드명, 특정 시설명)
- 결과가 전체의 0.1~1% 수준
- 3글자 이상 한글 검색
- 가장 빠른 성능 필요

**추천 쿼리**:

```sql
-- 브랜드 검색
WHERE MATCH(name) AGAINST('하얏트')

-- 복합 키워드 (AND)
WHERE MATCH(name, address, description) 
AGAINST('+강남 +하얏트 +스위트' IN BOOLEAN MODE)
LIMIT 20;
```

### 7.3 언제 Ngram FULLTEXT를 사용할까?

**사용 케이스**:

- 2글자 한글 검색 필수 ("서울", "부산", "제주")
- 띄어쓰기 불규칙한 데이터
- 부분 문자열 검색 (자동완성)

**주의사항**:

- Natural Language Mode 사용 (Boolean Mode 피하기)
- LIMIT 반드시 사용
- 인덱스 크기 모니터링 (Stop-word 대비 3~5배)

**추천 쿼리**:

```sql
-- Ngram + Natural Language
WHERE MATCH(name, address) AGAINST('속초')
ORDER BY MATCH(name, address) AGAINST('속초')
DESC
    LIMIT 20;
```

### 7.4 최적화 전략

**1. LIMIT 필수 사용**

```sql
-- 비추천: 전체 결과 조회
WHERE MATCH(...) AGAINST('속초 오션뷰')  
-- 17만 개 반환, 1.78초

-- 추천: 페이지네이션
WHERE MATCH(...) AGAINST('속초 오션뷰')
LIMIT 20  
-- 20개만 반환, 0.05초 (35배 빠름)
```

**2. 관련성 점수 기반 정렬**

```sql
SELECT *,
       MATCH(name, address, description) AGAINST('검색어') as score
FROM accommodations
WHERE MATCH(name, address, description) AGAINST('검색어')
ORDER BY score DESC, rating DESC
LIMIT 20;
```

**3. 2단계 검색 전략**

```sql
-- 1차: 엄격한 AND 검색
SELECT *
FROM accommodations
WHERE MATCH(name, address, description)
            AGAINST('+속초 +오션뷰' IN BOOLEAN MODE)
LIMIT 20;

-- 결과 부족 시 (예: 5개 미만) 자동으로 OR 검색
SELECT *
FROM accommodations
WHERE MATCH(name, address, description)
            AGAINST('속초 오션뷰')
ORDER BY MATCH(name, address, description) AGAINST('속초 오션뷰') DESC
LIMIT 20;
```

**4. 일반 인덱스와 병행**

```sql
-- FULLTEXT + 일반 인덱스 조합
WHERE MATCH(name, address, description) AGAINST('오션뷰')
  AND region_id = 123  -- 지역 필터 (일반 인덱스)
  AND price BETWEEN 50000 AND 150000  -- 가격 필터
ORDER BY rating
DESC
    LIMIT 20;
```

**5. 캐싱 전략 추가**

인기 검색어는 Redis 캐싱으로 추가 최적화:

```
키: "search:속초:오션뷰:page:1"
값: [숙소ID 리스트 상위 100개]
TTL: 1시간
```

## 8. 성능 개선 효과 요약

### 8.1 핵심 개선 지표

| 시나리오           | 개선 전 (LIKE)   | 개선 후 (FULLTEXT) | 성능 향상       |
|----------------|---------------|-----------------|-------------|
| 복합 키워드 + LIMIT | 1,072ms       | 37.8ms          | **28배**     |
| 고유 브랜드명        | 1,270ms       | 10ms            | **127배**    |
| 평균 검색 시간       | 1,000~1,500ms | 10~50ms         | **20~100배** |

### 8.2 사용자 경험 개선

**검색 응답 시간**:

- 개선 전: 1~1.5초 (사용자가 체감할 정도로 느림)
- 개선 후: 0.01~0.05초 (즉각 응답)

**검색 정확도**:

- LIKE: 정확한 문자열만 매칭, 표기 변형 누락
- FULLTEXT: 단어 단위 매칭, 유연한 검색

**페이지네이션**:

- LIKE: LIMIT 사용 시에도 Full Scan 필요
- FULLTEXT: LIMIT 사용 시 조기 종료, 극적인 성능 향상

## 결론

- LIKE 연산은 구현이 간단하지만, 대용량 데이터에서 정확한 문자열 매칭만 가능하여 실용성이 낮다.
- FULLTEXT INDEX는 키워드 분리, 관련성 점수, 빠른 검색 속도를 제공하며, 특히 **결과 비율 0.1~1% 구간에서 10~100배 이상의 성능 향상**을 보여준다.
- Ngram Parser 기반 FULLTEXT INDEX를 도입하여 **평균 검색 시간을 1초 이상에서 0.01~0.05초 수준으로 개선**했으며, 이는 사용자 경험을 크게 향상된다.
- 핵심 성공 요인은 **적절한 파서 선택 + LIMIT 사용 + 결과 비율 관리**입니다. 이 조건들을 충족하면 FULLTEXT INDEX는 텍스트 검색에서 충분히 효율적이다.
