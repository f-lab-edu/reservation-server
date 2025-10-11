package com.f1v3.reservation.api.auth;

import com.f1v3.reservation.api.auth.dto.LoginRequest;
import com.f1v3.reservation.api.auth.dto.LoginResponse;
import com.f1v3.reservation.auth.web.user.Login;
import com.f1v3.reservation.auth.web.user.LoginUser;
import com.f1v3.reservation.common.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import static com.f1v3.reservation.auth.token.TokenConstants.ACCESS_TOKEN_PREFIX;
import static com.f1v3.reservation.auth.token.TokenConstants.REFRESH_TOKEN_COOKIE_NAME;

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

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, response.refreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        // todo: 응답 body 프론트와 협의 필요
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .header(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN_PREFIX + response.accessToken())
                .body(ApiResponse.success(response));
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(@Login LoginUser user) {
        authService.logout(user.id());

        ResponseCookie deleteCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @GetMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponse>> reissue(@CookieValue(REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        LoginResponse response = authService.reissue(refreshToken);

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, response.refreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .header(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN_PREFIX + response.accessToken())
                .body(ApiResponse.success(response));
    }
}
