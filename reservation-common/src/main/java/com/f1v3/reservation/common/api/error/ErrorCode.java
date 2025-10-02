package com.f1v3.reservation.common.api.error;

import lombok.Getter;

/**
 * API 공통 에러 코드 ENUM
 *
 * @author Seungjo, Jeong
 */
@Getter
public enum ErrorCode {

    /*
    공통 에러 정의
     */
    INVALID_REQUEST_PARAMETER(4000, "요청 값이 올바르지 않습니다. 요청 값을 확인해주세요."),

    /*
    약관(Term) [code: 41xx]
     */


    /*
    핸드폰 인증(PhoneVerification) [code: 42xx]
     */


    /*
    회원(User) [code: 43xx]
     */


    /*
    서버 에러 정의
     */
    SERVER_ERROR(5000, "서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요."),
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    }
