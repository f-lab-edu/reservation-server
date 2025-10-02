# 핸드폰 인증(Phone Verification)

- 회원 가입 전, 핸드폰 번호 인증을 통해 올바른 회원의 가입을 유도할 수 있다.
- 핸드폰 인증은 SMS를 통해 이루어진다. (현재 단계에서는 로그를 통해 인증번호를 확인하는 방식으로 구현한다.)
- 핸드폰 인증은 다음과 같은 절차로 이루어진다.
    - 사용자가 핸드폰 번호를 입력한다.
    - 입력한 핸드폰 번호로 인증번호가 발송된다.
    - 사용자가 인증번호를 입력하고 확인 버튼을 누른다.
    - 인증번호에 대한 검증을 수행한다.
- 인증이 성공한다면 회원 가입 페이지로 이동한다.

이 때, 인증번호에 대한 검증(시스템 내부적으로)은 다음과 같은 절차로 이루어진다.

1. 핸드폰 번호와 인증번호가 일치하는지 확인한다.
2. 인증번호가 유효한지 확인한다. (발급 후 3분)
3. 인증 시도 횟수가 임계치를 넘지 않았는지 확인한다. (최대 3회)
4. 가입된 회원 중 해당 핸드폰 번호가 존재하는지 확인한다.
5. 모든 검증이 통과한다면 인증 성공으로 처리한다.

## 핸드폰 인증 데이터 모델링

```sql
CREATE TABLE phone_verifications
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone_number  VARCHAR(20) NOT NULL,
    verification_code   VARCHAR(5)  NOT NULL,
    attempt_count INT         NOT NULL DEFAULT 0, # 인증 시도 횟수 (최대 3회 제한)
    is_verified   BOOLEAN     NOT NULL DEFAULT FALSE,
    expired_at    TIMESTAMP   NOT NULL,
    verified_at   TIMESTAMP   NULL,
    created_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```