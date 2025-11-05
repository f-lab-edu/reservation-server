package com.f1v3.reservation.supplier.room;

import com.f1v3.reservation.common.api.response.ApiResponse;
import com.f1v3.reservation.supplier.room.dto.CreateRoomTypeRequest;
import com.f1v3.reservation.supplier.room.dto.CreateRoomTypeResponse;
import com.f1v3.reservation.supplier.room.dto.RoomTypeResponse;
import com.f1v3.reservation.supplier.room.dto.UpdateRoomTypeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 객실 타입 컨트롤러 - 공급자용
 *
 * @author Seungjo, Jeong
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/supplier/accommodations/{accommodationId}/room-types")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateRoomTypeResponse> createRoomType(
            @PathVariable Long accommodationId,
            @Valid @RequestBody CreateRoomTypeRequest request
    ) {
        CreateRoomTypeResponse response = roomTypeService.create(accommodationId, request);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<RoomTypeResponse>> getRoomTypes(
            @PathVariable Long accommodationId
    ) {
        List<RoomTypeResponse> response = roomTypeService.getRoomTypes(accommodationId);
        return ApiResponse.success(response);
    }

    @GetMapping("/{roomTypeId}")
    public ApiResponse<RoomTypeResponse> getRoomType(
            @PathVariable Long accommodationId,
            @PathVariable Long roomTypeId
    ) {
        RoomTypeResponse response = roomTypeService.getRoomType(accommodationId, roomTypeId);
        return ApiResponse.success(response);
    }

    @PutMapping("/{roomTypeId}")
    public void updateRoomType(
            @PathVariable Long accommodationId,
            @PathVariable Long roomTypeId,
            @RequestBody UpdateRoomTypeRequest request
    ) {
        roomTypeService.update(accommodationId, roomTypeId, request);
    }

    @DeleteMapping("/{roomTypeId}")
    public void deleteRoomType(
            @PathVariable Long accommodationId,
            @PathVariable Long roomTypeId
    ) {
        roomTypeService.delete(accommodationId, roomTypeId);
    }

}
