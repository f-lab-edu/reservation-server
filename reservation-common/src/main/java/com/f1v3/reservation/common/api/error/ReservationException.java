package com.f1v3.reservation.common.api.error;

import com.google.common.base.Joiner;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 공통 예외 최상위 클래스
 *
 * @author Seungjo, Jeong
 */
@Getter
public class ReservationException extends RuntimeException {
    private final ErrorStatus status;
    private final int code;
    private final String message;
    private final Consumer<String> logLevel;
    private final Map<String, Object> parameters;
    private final Exception rootCause;

    // 필수 정보: status, code, message, logLevel
    public ReservationException(ErrorCode errorCode, Consumer<String> logLevel, Map<String, Object> parameters, Exception rootCause) {
        super(errorCode.getMessage(), rootCause);
        this.status = errorCode.getStatus();
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.logLevel = logLevel;
        this.parameters = parameters;
        this.rootCause = rootCause;
    }

    public ReservationException(ErrorCode errorCode, Consumer<String> logLevel, Map<String, Object> parameters) {
        this(errorCode, logLevel, parameters, null);
    }

    public ReservationException(ErrorCode errorCode, Consumer<String> logLevel) {
        this(errorCode, logLevel, Collections.emptyMap(), null);
    }

    public String getLogMessage() {
        return Joiner.on(" | ")
                .skipNulls()
                .join(
                        "ErrorCode = " + code,
                        "Message = " + message,
                        parameters.isEmpty() ? null : "Parameters =  " + parameters,
                        rootCause == null ? null : "Root Cause =  " + ExceptionUtils.getRootCauseMessage(rootCause),
                        rootCause == null ? null : "Stack Trace = " + ExceptionUtils.getStackTrace(rootCause)
                );
    }
}
