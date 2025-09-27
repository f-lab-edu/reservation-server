package com.f1v3.reservation.admin.term.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 약관 버전 생성 요청 DTO
 *
 * @author Seungjo, Jeong
 */
public record CreateTermVersionRequest(

        @NotBlank(message = "약관 내용은 필수입니다.")
        String content,

        boolean isCurrent,

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime effectiveDateTime,

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime expiryDateTime
) {
}

