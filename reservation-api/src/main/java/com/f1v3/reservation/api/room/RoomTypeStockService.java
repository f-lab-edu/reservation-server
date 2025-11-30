package com.f1v3.reservation.api.room;

import com.f1v3.reservation.common.domain.room.RoomType;
import com.f1v3.reservation.common.domain.room.RoomTypeStock;
import com.f1v3.reservation.common.domain.room.repository.RoomTypeStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

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
     * 주어진 객실 타입과 숙박일자에 대해 재고 엔티티가 모두 존재하는지 확인하고, 존재하지 않는 경우 생성한다.
     * reservedCount 기반으로만 초기화하며, 가용성 검증/차감은 별도 단계에서 수행한다.
     */
    @Transactional
    public List<RoomTypeStock> ensureStocks(RoomType roomType, List<LocalDate> stayDays) {
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
                        .reservedCount(0)
                        .build())
                .toList();

        if (!created.isEmpty()) {
            try {
                roomTypeStockRepository.saveAll(created);
                roomTypeStockRepository.flush();
                exist.addAll(created);
            } catch (DataIntegrityViolationException e) {
                log.warn("RoomTypeStock already exists for roomTypeId={} on dates={}", roomType.getId(), missing, e);
                exist = roomTypeStockRepository.findAllByRoomTypeIdAndTargetDates(roomType.getId(), stayDays);
            }
        }

        return exist;
    }
}
