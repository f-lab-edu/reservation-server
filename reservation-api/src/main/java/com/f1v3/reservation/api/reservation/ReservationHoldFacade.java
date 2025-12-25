package com.f1v3.reservation.api.reservation;

import com.f1v3.reservation.api.reservation.dto.CreateReservationHoldRequest;
import com.f1v3.reservation.api.reservation.dto.ReservationHoldResponse;
import com.f1v3.reservation.api.room.RoomTypeStockService;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.room.RoomType;
import com.f1v3.reservation.common.domain.room.RoomTypeStock;
import com.f1v3.reservation.common.domain.room.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.f1v3.reservation.common.api.error.ErrorCode.*;

/**
 * 가계약 퍼사드 레이어
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationHoldFacade {

    private static final long LOCK_WAIT_MILLIS = 5_000L;
    private static final long LOCK_LEASE_MILLIS = 3_000L;
    private static final String LOCK_KEY_FORMAT = "lock:room-type:%d:date:%s";

    private final ReservationHoldService reservationHoldService;
    private final RoomTypeStockService roomTypeStockService;
    private final RoomTypeRepository roomTypeRepository;
    private final RedissonClient redissonClient;
    private final ReservationService reservationService;

    /**
     * 가계약 생성 요청
     * 1. RoomType + 숙박일 단위로 Redisson MultiLock 획득(대기/보유 시간 제한)
     * 2. 일자별 RoomTypeStock 확보/보정 후 재고 차감 준비
     * 3. ReservationHold 생성 및 만료 시각 설정
     */
    public ReservationHoldResponse createReservationHold(Long userId, CreateReservationHoldRequest request) {


        validateDate(request.checkIn(), request.checkOut());

        // 1. 객실 타입 조회
        RoomType roomType = roomTypeRepository.findById(request.roomTypeId())
                .orElseThrow(() -> new ReservationException(ROOM_TYPE_NOT_FOUND, log::info));

        validateCapacity(roomType, request.capacity());

        // 2. 숙박 기간 리스트 생성
        List<LocalDate> stayDays = getStayDays(request.checkIn(), request.checkOut());

        // 3. 객실 타입의 숙박 일자별로 락 획득 (Multi-Lock)
        RLock lock = redissonClient.getMultiLock(stayDays.stream()
                .sorted() /* 식사하는 철학자 문제 방지 */
                .map(date -> redissonClient.getLock(lockKey(roomType.getId(), date)))
                .toArray(RLock[]::new));

        boolean locked = false;
        long startTime = System.currentTimeMillis();

        try {
            // 4. MultiLock 획득 시도
            locked = lock.tryLock(LOCK_WAIT_MILLIS, LOCK_LEASE_MILLIS, TimeUnit.MILLISECONDS);

            if (!locked) {
                throw new ReservationException(RESERVATION_LOCK_TIMEOUT, log::info);
            }

            // 5. 객실 재고 확보 및 검증(reservedCount 기준, holdCount는 별도 계산 예정)
            List<RoomTypeStock> stocks = roomTypeStockService.ensureStocks(roomType, stayDays);
            validateAvailability(request, stayDays, stocks);

            // 6. 임시 예약 생성 후 레디스에 저장
            return reservationHoldService.createHold(userId, roomType, request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Map<String, Object> parameters = Map.of("roomTypeId", request.roomTypeId(), "stayDays", stayDays);
            throw new ReservationException(RESERVATION_LOCK_TIMEOUT, log::warn, parameters, e);
        } finally {
            checkLockTime(startTime);
            if (locked && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.warn("락 해제를 실패했습니다. roomTypeId={}, stayDays={}", request.roomTypeId(), stayDays, e);
                }
            }
        }
    }

    /**
     * 락 보유 시간 모니터링을 위한 메서드
     */
    private void checkLockTime(long holdStartTime) {
        // todo: 모니터링 시스템 연동 필요
        long elapsedTime = System.currentTimeMillis() - holdStartTime;
        if (elapsedTime > LOCK_LEASE_MILLIS) {
            log.warn("가계약 처리 시간이 락 보유 시간을 초과했습니다. elapsedTime = {}ms, lockLeaseTime = {}ms", elapsedTime, LOCK_LEASE_MILLIS);
        }
    }


    private List<LocalDate> getStayDays(LocalDate checkIn, LocalDate checkOut) {
        return checkIn.datesUntil(checkOut).toList();
    }

    private String lockKey(Long roomTypeId, LocalDate date) {
        return String.format(LOCK_KEY_FORMAT, roomTypeId, date);
    }

    private void validateDate(LocalDate checkIn, LocalDate checkOut) {
        // todo: 최대 예약일 제한 추가해야 함. (락의 범위가 너무 커져 성능 저하 우려)
        if (!checkIn.isBefore(checkOut)) {
            Map<String, Object> parameters = Map.of("checkIn", checkIn, "checkOut", checkOut);
            throw new ReservationException(INVALID_REQUEST_PARAMETER, log::info, parameters);
        }

        if (checkIn.plusDays(30).isAfter(checkOut)) {
            Map<String, Object> parameters = Map.of("checkIn", checkIn, "checkOut", checkOut);
            throw new ReservationException(RESERVATION_MAX_STAY_EXCEEDED, log::info, parameters);
        }
    }

    private void validateCapacity(RoomType roomType, Integer capacity) {
        if (capacity > roomType.getMaxCapacity()) {
            Map<String, Object> parameters = Map.of(
                    "requestedCapacity", capacity,
                    "maxCapacity", roomType.getMaxCapacity()
            );

            throw new ReservationException(ROOM_TYPE_CAPACITY_EXCEEDED, log::info, parameters);
        }
    }

    private void validateAvailability(CreateReservationHoldRequest request, List<LocalDate> stayDays, List<RoomTypeStock> stocks) {
        Map<LocalDate, RoomTypeStock> stockByDate = new HashMap<>();
        stocks.forEach(stock -> stockByDate.put(stock.getRoomTypeStockPk().getTargetDate(), stock));

        Map<LocalDate, Long> holdCounts = reservationHoldService.getHoldCounts(request.roomTypeId(), stayDays);

        for (LocalDate day : stayDays) {
            RoomTypeStock stock = stockByDate.get(day);
            if (stock == null) {
                Map<String, Object> parameters = Map.of(
                        "roomTypeId", request.roomTypeId(),
                        "missingDate", day
                );
                throw new ReservationException(ROOM_TYPE_STOCK_NOT_FOUND, log::info, parameters);
            }

            long holdCount = holdCounts.getOrDefault(day, 0L);
            long available = (long) stock.getTotalQuantity() - stock.getReservedCount() - holdCount;
            if (available < 1) {
                Map<String, Object> parameters = Map.of(
                        "roomTypeId", request.roomTypeId(),
                        "checkIn", request.checkIn(),
                        "checkOut", request.checkOut(),
                        "stayDays", stayDays,
                        "holdCount", holdCount
                );
                throw new ReservationException(ROOM_TYPE_STOCK_NOT_ENOUGH, log::info, parameters);
            }
        }
    }
}
