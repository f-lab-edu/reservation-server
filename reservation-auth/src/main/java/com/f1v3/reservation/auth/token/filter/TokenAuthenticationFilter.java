package com.f1v3.reservation.auth.token.filter;

import com.f1v3.reservation.auth.token.TokenProvider;
import com.f1v3.reservation.common.domain.user.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.f1v3.reservation.auth.token.TokenConstants.TOKEN_PREFIX;

/**
 * 토큰 인증 필터
 *
 * @author Seungjo, Jeong
 */
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (Objects.nonNull(tokenHeader) && tokenHeader.startsWith(TOKEN_PREFIX)) {
            String accessToken = tokenHeader.substring(TOKEN_PREFIX.length());
            if (tokenProvider.isTokenValid(accessToken)) {
                Long userId = tokenProvider.getUserId(accessToken);
                UserRole role = tokenProvider.getUserRole(accessToken);

                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
