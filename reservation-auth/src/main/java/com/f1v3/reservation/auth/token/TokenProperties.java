package com.f1v3.reservation.auth.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 토큰 설정 프로퍼티 클래스
 *
 * @author Seungjo, Jeong
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt.token")
public class TokenProperties {
    private String secretKey;
    private long accessTokenExpirationMs;
    private long refreshTokenExpirationMs;
}
