package com.f1v3.reservation.auth.token;

import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.user.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import static com.f1v3.reservation.auth.token.TokenConstants.KEY_ROLE;

/**
 * 토큰 생성 및 검증을 담당하는 클래스
 *
 * @author Seungjo, Jeong
 */
@Component
public class TokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public TokenProvider(TokenProperties tokenProperties) {
        this.secretKey = Keys.hmacShaKeyFor(tokenProperties.getSecretKey().getBytes());
        this.accessTokenExpirationMs = tokenProperties.getAccessTokenExpirationMs();
        this.refreshTokenExpirationMs = tokenProperties.getRefreshTokenExpirationMs();
    }

    public String generateAccessToken(Long userId, UserRole role) {
        return createToken(userId, role, accessTokenExpirationMs);
    }

    public String generateRefreshToken(Long userId, UserRole role) {
        return createToken(userId, role, refreshTokenExpirationMs);
    }

    public boolean isTokenValid(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().after(new Date());
    }

    public UserRole getUserRole(String token) {
        String roleName = parseClaims(token).get(KEY_ROLE, String.class);
        return UserRole.valueOf(roleName);
    }

    public Long getUserId(String token) {
        String userId = parseClaims(token).getSubject();
        return Long.valueOf(userId);
    }

    private String createToken(Long userId, UserRole role, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim(KEY_ROLE, role.name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new ReservationException(ErrorCode.TOKEN_EXPIRED);
        } catch (MalformedJwtException e) {
            throw new ReservationException(ErrorCode.TOKEN_INVALID);
        } catch (SignatureException e) {
            throw new ReservationException(ErrorCode.TOKEN_SIGNATURE_INVALID);
        }
    }
}
