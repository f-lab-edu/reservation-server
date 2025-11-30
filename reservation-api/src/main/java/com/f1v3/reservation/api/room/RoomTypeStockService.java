package com.f1v3.reservation.api.room;

import com.f1v3.reservation.api.reservation.dto.CreateReservationHoldRequest;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.room.RoomType;
import com.f1v3.reservation.common.domain.room.RoomTypeStock;
import com.f1v3.reservation.common.domain.room.repository.RoomTypeStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.f1v3.reservation.common.api.error.ErrorCode.ROOM_TYPE_STOCK_NOT_ENOUGH;

/**
 * 객실 타입 재고 서비스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomTypeStockService {

    private final RoomTypeStockRepository roomTypeStockRepository;

    /**
     * 임시 예약을 위한 재고 확보 및 차감
     */
    @Transactional
    public void decreaseForHold(RoomType roomType, CreateReservationHoldRequest request, List<LocalDate> stayDays) {

        // 객실 재고 확보 (없는 날짜는 생성)
        List<RoomTypeStock> stocks = findOrCreateStocks(roomType, stayDays);

        if (stocks.stream().anyMatch(stock -> !stock.hasAvailable())) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("roomTypeId", request.roomTypeId());
            parameters.put("checkIn", request.checkIn());
            parameters.put("checkOut", request.checkOut());
            throw new ReservationException(ROOM_TYPE_STOCK_NOT_ENOUGH, log::info, parameters);
        }

        try {
            // 객실 재고 차감
            stocks.forEach(RoomTypeStock::decrease);
            roomTypeStockRepository.saveAll(stocks);
        } catch (IllegalStateException e) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("roomTypeId", request.roomTypeId());
            parameters.put("checkIn", request.checkIn());
            parameters.put("checkOut", request.checkOut());

            throw new ReservationException(ROOM_TYPE_STOCK_NOT_ENOUGH, log::error, parameters, e);
        }
    }

    /**
     * stayDays 전부에 대한 RoomTypeStock을 확보한다. 없는 날짜는 생성 후 반환한다.
     */
    private List<RoomTypeStock> findOrCreateStocks(RoomType roomType, List<LocalDate> stayDays) {
        List<RoomTypeStock> exist = roomTypeStockRepository.findAllByRoomTypeIdAndTargetDates(roomType.getId(), stayDays);
        if (exist.size() == stayDays.size()) {
            return exist;
        }

        List<LocalDate> missing = new LinkedList<>(stayDays);
        exist.forEach(stock -> missing.remove(stock.getRoomTypeStockPk().getTargetDate()));

        List<RoomTypeStock> created = missing.stream()
                .map(date -> RoomTypeStock.builder()
                        .roomTypeStockPk(new RoomTypeStock.RoomTypeStockPk(roomType.getId(), date))
                        .totalQuantity(roomType.getTotalRoomCount())
                        .availableQuantity(roomType.getTotalRoomCount())
                        .build())
                .toList();

        if (!created.isEmpty()) {
            try {
                roomTypeStockRepository.saveAll(created);
                roomTypeStockRepository.flush();
                exist.addAll(created);
            } catch (DataIntegrityViolationException e) {
                exist = roomTypeStockRepository.findAllByRoomTypeIdAndTargetDates(roomType.getId(), stayDays);
            }
        }

        return exist;
    }
}
