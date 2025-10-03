package com.f1v3.reservation.common.api.response;

/**
 * API 공통 응답 클래스
 *
 * @author Seungjo, Jeong
 */
public record ApiResponse<T>(
        int code,
        String message,
        T content
) {
    public static <T> ApiResponse<T> success(T content) {
        return new ApiResponse<>(0, null, content);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message, T content) {
        return new ApiResponse<>(code, message, content);
    }
}
