package com.f1v3.reservation.supplier.room.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * 객실 타입 수정 요청 DTO
 *
 * @author Seungjo, Jeong
 */
public record UpdateRoomTypeRequest(

        @NotBlank(message = "객실 이름을 입력해주세요.")
        String name,

        @NotBlank(message = "객실 설명을 입력해주세요.")
        String description,

        @Min(value = 1, message = "표준 인원은 최소 1명 이상이어야 합니다.")
        int standardCapacity,

        @Min(value = 1, message = "최대 인원은 최소 1명 이상이어야 합니다.")
        int maxCapacity,

        @Min(value = 1, message = "전체 객실 수는 최소 1개 이상이어야 합니다.")
        int totalRoomCount,

        @Min(value = 0, message = "기본 가격은 0원 이상이어야 합니다.")
        BigDecimal basePrice,

        String thumbnail // nullable
) {
}
