package com.f1v3.reservation.admin;

import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.api.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
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

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ApiResponse<?>> handleReservationException(ReservationException e) {
        ApiResponse<?> response = ApiResponse.error(e.getCode(), e.getMessage());

        return ResponseEntity
                .status(e.getStatus().getCode())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleArgumentNotValidException(MethodArgumentNotValidException e) {

        ErrorCode errorCode = ErrorCode.INVALID_REQUEST_PARAMETER;

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception e) {

        log.error("Unexpected error = {}", e.getMessage(), e);

        ErrorCode errorCode = ErrorCode.SERVER_ERROR;

        ApiResponse<?> response = ApiResponse.error(
                errorCode.getCode(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus().getCode())
                .body(response);
    }
}
