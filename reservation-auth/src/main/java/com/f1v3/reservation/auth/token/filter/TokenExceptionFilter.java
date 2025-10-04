package com.f1v3.reservation.auth.token.filter;

import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.api.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 토큰 처리 중 발생하는 예외를 처리하는 필터
 *
 * @author Seungjo, Jeong
 */
@Component
@RequiredArgsConstructor
public class TokenExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ReservationException e) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getCode(), e.getMessage());
        } catch (Exception e) {
            ErrorCode errorCode = ErrorCode.SERVER_ERROR;
            setErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorCode.getCode(), errorCode.getMessage());
        }
    }

    private void setErrorResponse(HttpServletResponse response, int status, int code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), ApiResponse.error(code, message));
    }
}
