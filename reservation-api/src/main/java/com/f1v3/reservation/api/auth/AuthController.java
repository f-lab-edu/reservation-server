package com.f1v3.reservation.api.auth;

import com.f1v3.reservation.api.auth.dto.LoginRequest;
import com.f1v3.reservation.api.auth.dto.LoginResponse;
import com.f1v3.reservation.auth.token.TokenConstants;
import com.f1v3.reservation.common.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * 인증 API 컨트롤러
 *
 * @author Seungjo, Jeong
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        // todo: 응답 body 프론트와 협의 필요
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .header(HttpHeaders.AUTHORIZATION, TokenConstants.ACCESS_TOKEN_PREFIX + response.accessToken())
                .body(ApiResponse.success(response));
    }
}
