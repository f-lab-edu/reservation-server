package com.f1v3.reservation.admin.term.dto;

import lombok.Getter;

/**
 * 생성된 약관 응답 DTO.
 *
 * @author Seungjo, Jeong
 */
@Getter
public class CreateTermResponse {

    private final Long id;

    public CreateTermResponse(Long id) {
        this.id = id;
    }
}
