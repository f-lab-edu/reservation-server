package com.f1v3.reservation.api.reservation;

import com.f1v3.reservation.api.reservation.dto.ConfirmReservationHoldResponse;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.f1v3.reservation.common.api.error.ErrorCode.*;

/**
 * 임시 예약 퍼사드 레이어
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationHoldFacade {

    private static final long LOCK_WAIT_MILLIS = 1_000L;
    private static final long LOCK_LEASE_MILLIS = 5_000L;
    private static final String LOCK_KEY_FORMAT = "lock:room-type:%d:date:%s";

    private final ReservationHoldService reservationHoldService;
    private final RoomTypeStockService roomTypeStockService;
    private final RoomTypeRepository roomTypeRepository;
    private final RedissonClient redissonClient;
    private final ReservationService reservationService;

    /**
     * 임시 예약 생성 요청
     * 1. RoomType+숙박일 단위로 Redisson MultiLock 획득(대기/보유 시간 제한)
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
                .sorted()
                .map(date -> redissonClient.getLock(lockKey(roomType.getId(), date)))
                .toArray(RLock[]::new));

        boolean locked = false;

        try {
            // 4. MultiLock 획득 시도 (대기 시간, 보유 시간, 단위 설정)
            locked = lock.tryLock(LOCK_WAIT_MILLIS, LOCK_LEASE_MILLIS, TimeUnit.MILLISECONDS);

            if (!locked) {
                throw new ReservationException(RESERVATION_LOCK_TIMEOUT, log::info);
            }

            // 5. 객실 재고 확보 및 검증(reservedCount 기준, holdCount는 별도 계산 예정)
            List<RoomTypeStock> stocks = roomTypeStockService.ensureStocks(roomType, stayDays);
            validateAvailability(request, stayDays, stocks);

            // 6. 임시 예약 생성 후 레디스에 저장
            return reservationHoldService.createHold(
                    userId,
                    roomType,
                    request
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReservationException(RESERVATION_LOCK_TIMEOUT, log::warn,
                    Map.of("roomTypeId", request.roomTypeId(), "stayDays", stayDays), e);
        } finally {
            if (locked) {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.warn("Failed to unlock reservation hold lock. roomTypeId={}, stayDays={}", request.roomTypeId(), stayDays, e);
                }
            }
        }
    }

    /**
     * 임시 예약 확인 및 예약 확정 처리 (결제가 완료된 경우 호출하는 것을 전제)
     * 1. Redis에서 임시 예약 정보 조회 및 검증 (존재 여부, 만료 여부)
     * 2. 임시 예약에 해당하는 재고 확정 처리 및 예약 생성
     *
     */
    public void confirmReservationHold(String holdKey, Long userId) {
        ConfirmReservationHoldResponse response = reservationHoldService.confirmReservationHold(holdKey, userId);

        RoomType roomType = roomTypeRepository.findById(response.roomTypeId())
                .orElseThrow(() -> new ReservationException(ROOM_TYPE_NOT_FOUND, log::info));

        List<LocalDate> stayDays = getStayDays(response.checkIn(), response.checkOut());

        RLock lock = redissonClient.getMultiLock(stayDays.stream()
                .sorted()
                .map(date -> redissonClient.getLock(lockKey(roomType.getId(), date)))
                .toArray(RLock[]::new));

        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_WAIT_MILLIS, LOCK_LEASE_MILLIS, TimeUnit.MILLISECONDS);

            if (!locked) {
                throw new ReservationException(RESERVATION_LOCK_TIMEOUT, log::info,
                        Map.of("roomTypeId", response.roomTypeId(), "stayDays", stayDays));
            }

            roomTypeStockService.reserve(roomType, stayDays, response.quantity());
            reservationService.confirmReservation(userId, response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReservationException(RESERVATION_LOCK_TIMEOUT, log::warn,
                    Map.of("roomTypeId", response.roomTypeId(), "stayDays", stayDays), e);
        } finally {
            if (locked) {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.warn("Failed to unlock reservation confirm lock. roomTypeId={}, stayDays={}", response.roomTypeId(), stayDays, e);
                }
            }
        }
    }

    private List<LocalDate> getStayDays(LocalDate request, LocalDate request1) {
        return Stream.iterate(request, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(request, request1))
                .toList();
    }

    private String lockKey(Long roomTypeId, LocalDate date) {
        return String.format(LOCK_KEY_FORMAT, roomTypeId, date);
    }

    private void validateDate(LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut)) {
            Map<String, Object> parameters = Map.of("checkIn", checkIn, "checkOut", checkOut);
            throw new ReservationException(INVALID_REQUEST_PARAMETER, log::info, parameters);
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
        boolean notEnough = stocks.stream().anyMatch(stock -> !stock.hasAvailable(1));
        if (notEnough) {
            Map<String, Object> parameters = Map.of(
                    "roomTypeId", request.roomTypeId(),
                    "checkIn", request.checkIn(),
                    "checkOut", request.checkOut(),
                    "stayDays", stayDays
            );
            throw new ReservationException(ROOM_TYPE_STOCK_NOT_ENOUGH, log::info, parameters);
        }
    }
}
