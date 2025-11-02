package com.f1v3.reservation.api.room;

import com.f1v3.reservation.api.room.dto.RoomTypeResponse;
import com.f1v3.reservation.common.api.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 객실 컨트롤러 클래스 (사용자용, 단순 조회만 가능)
 *
 * @author Seungjo, Jeong
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/accommodations/{accommodationId}/rooms")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{roomId}")
    public ApiResponse<RoomTypeResponse> getRoom(
            @PathVariable Long accommodationId,
            @PathVariable Long roomId
    ) {
        RoomTypeResponse response = roomService.getRoom(accommodationId, roomId);
        return ApiResponse.success(response);
    }

}
