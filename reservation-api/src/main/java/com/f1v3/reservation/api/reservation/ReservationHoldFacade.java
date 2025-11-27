package com.f1v3.reservation.api.reservation;

import com.f1v3.reservation.api.reservation.dto.CreateReservationHoldRequest;
import com.f1v3.reservation.api.reservation.dto.ReservationHoldResponse;
import com.f1v3.reservation.api.room.RoomTypeStockService;
import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.room.RoomType;
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
                .orElseThrow(() -> new ReservationException(ErrorCode.ROOM_TYPE_NOT_FOUND, log::info));

        validateCapacity(roomType, request.capacity());

        // 2. 숙박 기간 리스트 생성
        List<LocalDate> stayDays = Stream.iterate(request.checkIn(), date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(request.checkIn(), request.checkOut()))
                .toList();

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
                throw new ReservationException(ErrorCode.RESERVATION_LOCK_TIMEOUT, log::info,
                        Map.of("roomTypeId", request.roomTypeId(), "stayDays", stayDays));
            }

            // 5. 객실 재고 차감 수행
            roomTypeStockService.decreaseForHold(roomType, request, stayDays);

            // 6. 임시 예약 생성 후 레디스에 저장
            return reservationHoldService.createHold(
                    userId,
                    roomType,
                    request
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReservationException(ErrorCode.RESERVATION_LOCK_TIMEOUT, log::warn,
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

    private String lockKey(Long roomTypeId, LocalDate date) {
        return String.format(LOCK_KEY_FORMAT, roomTypeId, date);
    }

    private void validateDate(LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut)) {
            Map<String, Object> parameters = Map.of("checkIn", checkIn, "checkOut", checkOut);
            throw new ReservationException(ErrorCode.INVALID_REQUEST_PARAMETER, log::info, parameters);
        }
    }

    private void validateCapacity(RoomType roomType, Integer capacity) {
        if (capacity > roomType.getMaxCapacity()) {
            Map<String, Object> parameters = Map.of(
                    "requestedCapacity", capacity,
                    "maxCapacity", roomType.getMaxCapacity()
            );

            throw new ReservationException(ErrorCode.ROOM_TYPE_CAPACITY_EXCEEDED, log::info, parameters);
        }
    }

}
