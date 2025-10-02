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
    공통 에러 정의 [code: 1xxx]
     */
    INVALID_REQUEST_PARAMETER(ErrorStatus.BAD_REQUEST, 1000, "요청 값이 올바르지 않습니다. 요청 값을 확인해주세요."),

    /*
    약관(Term) [code: 41xx]
     */
    TERM_NOT_FOUND(ErrorStatus.NOT_FOUND, 4101, "약관을 찾을 수 없습니다."),
    TERM_CODE_INVALID(ErrorStatus.BAD_REQUEST, 4102, "올바르지 않은 약관 코드입니다."),
    TERM_REQUIRED_NOT_AGREED(ErrorStatus.BAD_REQUEST, 4103, "필수 약관에 동의하지 않았습니다."),
    TERM_VERSION_CONSTRAINT_VIOLATION(ErrorStatus.CONFLICT, 4104, "약관 생성 시 버전 충돌이 발생했습니다. 버전을 확인해주세요."),

    /*
    핸드폰 인증(PhoneVerification) [code: 42xx]
     */
    PHONE_VERIFICATION_NOT_FOUND(ErrorStatus.NOT_FOUND, 4201, "핸드폰 인증 정보를 찾을 수 없습니다."),
    PHONE_VERIFICATION_CODE_EXPIRED(ErrorStatus.BAD_REQUEST, 4202, "인증번호가 만료되었습니다."),
    PHONE_VERIFICATION_CODE_INVALID(ErrorStatus.BAD_REQUEST, 4203, "인증번호가 올바르지 않습니다."),
    PHONE_VERIFICATION_ALREADY_VERIFIED(ErrorStatus.CONFLICT, 4204, "이미 인증된 핸드폰 번호입니다."),
    PHONE_VERIFICATION_ATTEMPTS_EXCEEDED(ErrorStatus.BAD_REQUEST, 4205, "인증 시도 횟수를 초과했습니다."),
    PHONE_VERIFICATION_NOT_VERIFIED(ErrorStatus.BAD_REQUEST, 4207, "핸드폰 인증이 완료되지 않았습니다."),
    PHONE_VERIFICATION_INFO_EXPIRED(ErrorStatus.BAD_REQUEST, 4208, "핸드폰 인증 정보가 만료되었습니다."),
    PHONE_VERIFICATION_RESEND_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, 4209, "인증번호 재전송은 3분 후에 가능합니다."),

    /*
    회원(User) [code: 43xx]
     */
    USER_EMAIL_ALREADY_EXISTS(ErrorStatus.CONFLICT, 4301, "이미 등록된 이메일입니다."),
    USER_PHONE_ALREADY_EXISTS(ErrorStatus.CONFLICT, 4302, "이미 등록된 핸드폰 번호입니다."),

    /*
    서버 에러 정의
     */
    SERVER_ERROR(ErrorStatus.INTERNAL_SERVER_ERROR, 5000, "서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요."),
    ;

    private final ErrorStatus status;
    private final int code;
    private final String message;

    ErrorCode(ErrorStatus status, int code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}