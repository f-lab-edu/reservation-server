package com.f1v3.reservation.admin.term.dto;

import jakarta.validation.constraints.Min;
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

        @Min(value = 1, message = "버전은 1 이상의 값이어야 합니다.")
        int version,

        boolean isCurrent,

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime effectiveDateTime,

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime expiryDateTime
) {
}

