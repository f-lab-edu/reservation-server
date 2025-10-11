package com.f1v3.reservation.common.api.error;

import lombok.Getter;

/**
 * HTTP 에러 상태 코드
 *
 * @author Seungjo, Jeong
 */
@Getter
public enum ErrorStatus {

    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    CONFLICT(409),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    ErrorStatus(int code) {
        this.code = code;
    }
}
