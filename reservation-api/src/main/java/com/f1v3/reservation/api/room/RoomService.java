package com.f1v3.reservation.api.room;

import com.f1v3.reservation.api.room.dto.RoomTypeResponse;
import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.accommodation.repository.AccommodationRepository;
import com.f1v3.reservation.common.domain.room.RoomType;
import com.f1v3.reservation.common.domain.room.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 객실 서비스 클래스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final AccommodationRepository accommodationRepository;
    private final RoomTypeRepository roomTypeRepository;

    public RoomTypeResponse getRoom(Long accommodationId, Long roomId) {

        if (!accommodationRepository.existsById(accommodationId)) {
            throw  new ReservationException(ErrorCode.ACCOMMODATION_NOT_FOUND, log::info);
        }

        RoomType roomType = roomTypeRepository.findById(roomId)
                .orElseThrow(() -> new ReservationException(ErrorCode.ROOM_TYPE_NOT_FOUND, log::info));

        return RoomTypeResponse.from(roomType);
    }
}
