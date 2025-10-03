package com.f1v3.reservation.api;

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
 * 사용자 서버 컨트롤러 어드바이스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@RestControllerAdvice
public class ReservationControllerAdvice {

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ApiResponse<?>> handleReservationException(ReservationException e) {
        return ResponseEntity
                .status(e.getStatus().getCode())
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleArgumentNotValidException(MethodArgumentNotValidException e) {

        ErrorCode code = ErrorCode.INVALID_REQUEST_PARAMETER;

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ApiResponse<?> response = ApiResponse.error(
                ErrorCode.INVALID_REQUEST_PARAMETER.getCode(),
                ErrorCode.INVALID_REQUEST_PARAMETER.getMessage(),
                errors
        );

        return ResponseEntity
                .status(code.getStatus().getCode())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception e) {
        log.info("Unexpected error = {}", e.getMessage(), e);

        ErrorCode code = ErrorCode.SERVER_ERROR;

        ApiResponse<?> response = ApiResponse.error(
                code.getCode(),
                code.getMessage()
        );

        return ResponseEntity
                .status(code.getStatus().getCode())
                .body(response);
    }
}
