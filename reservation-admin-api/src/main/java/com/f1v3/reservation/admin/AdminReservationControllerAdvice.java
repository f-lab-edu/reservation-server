package com.f1v3.reservation.admin;

import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.api.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 관리자 서버 컨트롤러 어드바이스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@RestControllerAdvice
public class AdminReservationControllerAdvice {

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ApiResponse<?>> handleReservationException(ReservationException e) {
        String logMessage = e.getLogMessage();
        e.getLogLevel().accept(logMessage);

        return ResponseEntity
                .status(e.getStatus().getCode())
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        ErrorCode errorCode = ErrorCode.INVALID_REQUEST_PARAMETER;

        Map<String, String> errors = new HashMap<>();
        Map<String, Object> parameters = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
                    errors.put(error.getField(), error.getDefaultMessage());
                    parameters.put(error.getField(), error.getRejectedValue());
                }
        );

        log.info("Validation error | URL: {} | Method: {} | Errors: {} | Parameters: {}",
                request.getRequestURI(),
                request.getMethod(),
                errors,
                parameters
        );

        ApiResponse<?> response = ApiResponse.error(
                errorCode.getCode(),
                errorCode.getMessage(),
                errors
        );

        return ResponseEntity
                .status(errorCode.getStatus().getCode())
                .body(response);
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(
            Exception e, HttpServletRequest request) {

        log.error("Unexpected error | URL: {} | Method: {} | Error: {}",
                request.getRequestURI(),
                request.getMethod(),
                e.getMessage(),
                e
        );

        ErrorCode errorCode = ErrorCode.SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus().getCode())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }
}
