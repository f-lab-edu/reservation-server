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
    약관(Term) [code: 2xxx]
     */
    TERM_NOT_FOUND(ErrorStatus.NOT_FOUND, 2001, "약관을 찾을 수 없습니다."),
    TERM_CODE_INVALID(ErrorStatus.BAD_REQUEST, 2002, "올바르지 않은 약관 코드입니다."),
    TERM_REQUIRED_NOT_AGREED(ErrorStatus.BAD_REQUEST, 2003, "필수 약관에 동의하지 않았습니다."),
    TERM_VERSION_CONSTRAINT_VIOLATION(ErrorStatus.CONFLICT, 2004, "약관 생성 시 버전 충돌이 발생했습니다. 버전을 확인해주세요."),

    /*
    핸드폰 인증(PhoneVerification) [code: 3xxx]
     */
    PHONE_VERIFICATION_NOT_FOUND(ErrorStatus.NOT_FOUND, 3001, "핸드폰 인증 정보를 찾을 수 없습니다."),
    PHONE_VERIFICATION_CODE_EXPIRED(ErrorStatus.BAD_REQUEST, 3002, "인증번호가 만료되었습니다."),
    PHONE_VERIFICATION_CODE_INVALID(ErrorStatus.BAD_REQUEST, 3003, "인증번호가 올바르지 않습니다."),
    PHONE_VERIFICATION_ALREADY_VERIFIED(ErrorStatus.CONFLICT, 3004, "이미 인증된 핸드폰 번호입니다."),
    PHONE_VERIFICATION_ATTEMPTS_EXCEEDED(ErrorStatus.BAD_REQUEST, 3005, "인증 시도 횟수를 초과했습니다."),
    PHONE_VERIFICATION_NOT_VERIFIED(ErrorStatus.BAD_REQUEST, 3007, "핸드폰 인증이 완료되지 않았습니다."),
    PHONE_VERIFICATION_INFO_EXPIRED(ErrorStatus.BAD_REQUEST, 3008, "핸드폰 인증 정보가 만료되었습니다."),
    PHONE_VERIFICATION_RESEND_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, 3009, "인증번호 재전송은 3분 후에 가능합니다."),

    /*
    회원(User) [code: 4xxx]
     */
    USER_EMAIL_ALREADY_EXISTS(ErrorStatus.CONFLICT, 4001, "이미 등록된 이메일입니다."),
    USER_PHONE_ALREADY_EXISTS(ErrorStatus.CONFLICT, 4002, "이미 등록된 핸드폰 번호입니다."),
    USER_NOT_FOUND(ErrorStatus.NOT_FOUND, 4003, "사용자를 찾을 수 없습니다."),

    /*
    인증/인가 (Auth) [code: 5xxx]
     */
    TOKEN_EXPIRED(ErrorStatus.UNAUTHORIZED, 5001, "토큰이 만료되었습니다. 다시 로그인해주세요."),
    TOKEN_INVALID(ErrorStatus.UNAUTHORIZED, 5002, "유효하지 않은 토큰입니다. 다시 로그인해주세요."),
    TOKEN_SIGNATURE_INVALID(ErrorStatus.UNAUTHORIZED, 5003, "토큰 시그니처가 유효하지 않습니다. 다시 로그인해주세요."),

    UNAUTHORIZED(ErrorStatus.UNAUTHORIZED, 5004, "인증이 필요합니다. 로그인 후 다시 시도해주세요."),

    /*
    숙소(Accommodation) [code: 6xxx]
     */
    ACCOMMODATION_NOT_FOUND(ErrorStatus.NOT_FOUND, 6001, "숙소를 찾을 수 없습니다."),
    ACCOMMODATION_ACCESS_DENIED(ErrorStatus.FORBIDDEN, 6002, "숙소 소유자만 수정/삭제할 수 있습니다."),

    /*
    객실 타입(Room Type) [code: 7xxx]
     */
    ROOM_TYPE_NOT_FOUND(ErrorStatus.NOT_FOUND, 7001, "객실 타입을 찾을 수 없습니다."),

    /*
    서버 에러 정의 [code: 9xxx]
     */
    SERVER_ERROR(ErrorStatus.INTERNAL_SERVER_ERROR, 9000, "서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요."),
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
