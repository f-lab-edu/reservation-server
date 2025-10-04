package com.f1v3.reservation.auth.token;

/**
 * 토큰 관련 상수 유틸리티 클래스
 *
 * @author Seungjo, Jeong
 */
public class TokenConstants {

    private TokenConstants() {
    }

    public static final String ACCESS_TOKEN_PREFIX = "Bearer ";

    public static final String KEY_ROLE = "role";

    public static final String[] PUBLIC_URLS = {
            "/v1/phone-verifications/**",
            "/v1/terms/**",
            "/v1/users/signup",
            "/v1/auth/**",
    };
}
