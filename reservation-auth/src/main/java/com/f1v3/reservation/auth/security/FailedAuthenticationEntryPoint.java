package com.f1v3.reservation.auth.security;

import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 사용자 접근을 처리하는 Entry Point
 *
 * @author Seungjo, Jeong
 */
@Component
@RequiredArgsConstructor
public class FailedAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorCode error = ErrorCode.UNAUTHORIZED;

        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.error(error.getCode(), error.getMessage())
        );
    }
}
