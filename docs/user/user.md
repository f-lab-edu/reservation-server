# 회원(User)

## 1. 목적
예약 서비스 사용자, 공급자, 관리자를 단일 사용자 테이블에서 관리하면서도 역할 기반 접근 제어를 적용하기 위한 도메인 정의다. 가입, 프로필 관리, 권한 부여, 약관 동의 이력을 포함한다.

## 2. 요구사항
|항목|내용|
|---|---|
|가입 경로|휴대폰 인증을 통과한 뒤 이메일/비밀번호/개인정보를 입력하고 필수 약관에 동의해야 한다.|
|역할|`USER`, `SUPPLIER`, `ADMIN` 세 가지. Access Token에 포함되어 각 API Gateway/Security Filter에서 권한을 결정한다.|
|프로필 관리|회원은 닉네임, 연락처(검증 절차 포함), 알림 설정을 수정할 수 있다. 공급자/관리자는 별도 어드민에서 속성 편집.|
|중복 검사|이메일/전화번호는 각각 유일해야 하며, 공급자 전환 시 기존 USER 레코드에 역할을 추가하는 방식을 우선 검토한다.|
|비밀번호 정책|BCrypt 해시 보관, 최소 길이 8자 + 영문/숫자 조합.|
|약관 동의|가입 시 선택한 버전 정보를 `user_term_agreements`에 저장하여 감사 가능성을 확보한다.|

## 3. 데이터 모델
```sql
CREATE TABLE users (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_password  VARCHAR(60)  NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    nickname       VARCHAR(50)  NOT NULL,
    phone_number   VARCHAR(20)  NOT NULL UNIQUE,
    birth_date     DATE         NOT NULL,
    gender         VARCHAR(10)  NOT NULL,  -- 'MALE', 'FEMALE', 'UNKNOWN'
    role           VARCHAR(20)  NOT NULL,  -- USER / SUPPLIER / ADMIN
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    last_login_at  TIMESTAMP NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
- 향후 다중 역할을 지원해야 할 경우 `user_roles` 조인 테이블 도입을 검토한다.

### 약관 동의
`user_term_agreements` 구조는 [docs/terms/terms.md](../terms/terms.md) 참고.

## 4. 운영 시나리오
1. **일반 가입**: 휴대폰 인증 성공 → 약관 목록 조회 → 필수 항목 동의 확인 → 사용자 생성 → Access/Refresh Token 발급.
2. **공급자 전환**: 기존 USER가 공급자 신청을 하면 추가 심사 후 `role`을 `SUPPLIER`로 업데이트하고, 공급자 상세 정보는 별도 `suppliers` 테이블에 저장한다.
3. **계정 비활성화**: 이용 제한 시 `status = SUSPENDED`로 변경하고, 로그인 시 Security Filter에서 차단한다.
4. **감사 로깅**: 이메일, 전화번호 변경, 역할 변경은 모두 감사 로그에 남기고 관리자 화면에서 검색 가능하도록 한다.
