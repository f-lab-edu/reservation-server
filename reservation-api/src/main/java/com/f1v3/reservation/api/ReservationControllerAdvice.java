package com.f1v3.reservation.api;

import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.api.response.ApiResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * API 예외 처리 컨트롤러 어드바이스
 *
 * @author Seungjo, Jeong
 */
@RestControllerAdvice
public class ReservationControllerAdvice {

    @ExceptionHandler(ReservationException.class)
    public ApiResponse<?> handleReservationException(ReservationException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleArgumentNotValidException(MethodArgumentNotValidException e) {

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ApiResponse.error(
                ErrorCode.INVALID_REQUEST_PARAMETER.getCode(),
                ErrorCode.INVALID_REQUEST_PARAMETER.getMessage(),
                errors
        );
    }
}
