package com.f1v3.reservation.api.auth.dto;

/**
 * 로그인 응답 DTO
 *
 * @author Seungjo, Jeong
 */
public record LoginResponse(
        // fixme: 테스트를 위한 임시 응답 객체 (원래는 헤더 및 쿠키만으로 전달)
        String accessToken,
        String refreshToken
) {
}
