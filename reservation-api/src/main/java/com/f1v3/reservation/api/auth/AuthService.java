package com.f1v3.reservation.api.auth;

import com.f1v3.reservation.api.auth.dto.LoginRequest;
import com.f1v3.reservation.api.auth.dto.LoginResponse;
import com.f1v3.reservation.auth.token.TokenProperties;
import com.f1v3.reservation.auth.token.TokenProvider;
import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.user.User;
import com.f1v3.reservation.common.domain.user.repository.UserRepository;
import com.f1v3.reservation.common.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 인증 서비스 클래스
 *
 * @author Seungjo, Jeong
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RedisRepository redisRepository;
    private final TokenProperties tokenProperties;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ReservationException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ReservationException(ErrorCode.USER_NOT_FOUND);
        }

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getRole());

        redisRepository.setValue(
                REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                Duration.ofMillis(tokenProperties.getRefreshTokenExpirationMs())
        );

        return new LoginResponse(accessToken, refreshToken);
    }

    public void logout(Long id) {
        redisRepository.deleteValue(REFRESH_TOKEN_PREFIX + id);
    }

    public LoginResponse reissue(String refreshToken) {
        if (!tokenProvider.isTokenValid(refreshToken)) {
            throw new ReservationException(ErrorCode.TOKEN_INVALID);
        }

        Long userId = tokenProvider.getUserId(refreshToken);
        String savedRefreshToken = redisRepository.getValue(REFRESH_TOKEN_PREFIX + userId);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new ReservationException(ErrorCode.TOKEN_INVALID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ReservationException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getRole());
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getRole());

        redisRepository.setValue(
                REFRESH_TOKEN_PREFIX + user.getId(),
                newRefreshToken,
                Duration.ofMillis(tokenProperties.getRefreshTokenExpirationMs())
        );

        return new LoginResponse(newAccessToken, newRefreshToken);

    }
}
