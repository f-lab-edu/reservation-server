package com.f1v3.reservation.common.api.error;

import lombok.Getter;

/**
 * 공통 예외 최상위 클래스
 *
 * @author Seungjo, Jeong
 */
@Getter
public class ReservationException extends RuntimeException {
    private final int code;
    private final String message;

    public ReservationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}

