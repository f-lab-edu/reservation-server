package com.f1v3.reservation.api.reservation;

import com.f1v3.reservation.api.reservation.cache.ReservationHoldCache;
import com.f1v3.reservation.api.reservation.cache.ReservationHoldCountCache;
import com.f1v3.reservation.api.reservation.cache.ReservationHoldIdempotencyCache;
import com.f1v3.reservation.api.reservation.cache.ReservationHoldIndexCache;
import com.f1v3.reservation.api.reservation.dto.ConfirmReservationHoldResponse;
import com.f1v3.reservation.api.reservation.dto.CreateReservationHoldRequest;
import com.f1v3.reservation.api.reservation.dto.ReservationHoldResponse;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.reservation.ReservationHold;
import com.f1v3.reservation.common.domain.room.RoomType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.f1v3.reservation.common.api.error.ErrorCode.*;

/**
 * 가계약 서비스 레이어
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationHoldService {

    //    private static final Duration HOLD_EXPIRE_MINUTES = Duration.ofMinutes(10);
    private static final Duration HOLD_EXPIRE_MINUTES = Duration.ofMinutes(1);

    private final ReservationHoldCache holdCache;
    private final ReservationHoldIndexCache indexCache;
    private final ReservationHoldIdempotencyCache idempotencyCache;
    private final ReservationHoldCountCache countCache;

    /**
     * 가계약 생성 후 Redis JSON 저장 (TTL 기반, 만료 배치 없이 처리)
     */
    public ReservationHoldResponse createHold(Long userId, RoomType roomType, CreateReservationHoldRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plus(HOLD_EXPIRE_MINUTES);
        LocalDate checkIn = request.checkIn();
        LocalDate checkOut = request.checkOut();
        List<LocalDate> stayDays = stayDays(checkIn, checkOut);

        // 1. 기존 가계약 정리 (같은 사용자가 동일 객실타입/기간으로 가계약을 여러개 생성하는 경우 방지)
        indexCache.findHoldId(userId, roomType.getId(), checkIn, checkOut)
                .ifPresent(existingHoldId -> cleanupExistingHold(existingHoldId, roomType.getId(), userId, checkIn, checkOut));

        // 2. 가계약 생성
        String holdId = UUID.randomUUID().toString();
        ReservationHold hold = new ReservationHold(
                holdId,
                roomType.getId(),
                checkIn,
                checkOut,
                userId,
                request.idempotencyKey(),
                now,
                expiredAt
        );

        // 3. 멱등키 확인 (중복 요청 방지)
        checkIdempotencyKeyAvailable(request.idempotencyKey(), roomType.getId(), checkIn, checkOut, holdId);

        // fixme: 하나의 Lua Script로 처리하여 라운드 트립을 1회로 만들기
        //  (장애 발생시 정합성을 보장하는 방안을 고려해야 함.)
        holdCache.save(hold, HOLD_EXPIRE_MINUTES);
        indexCache.save(userId, roomType.getId(), checkIn, checkOut, holdId, HOLD_EXPIRE_MINUTES);
        idempotencyCache.save(request.idempotencyKey(), holdId, roomType.getId(), checkIn, checkOut, HOLD_EXPIRE_MINUTES);
        stayDays.forEach(day -> countCache.increment(roomType.getId(), day, HOLD_EXPIRE_MINUTES));

        return new ReservationHoldResponse(holdId, expiredAt);
    }

    public ConfirmReservationHoldResponse confirmReservationHold(String holdId, Long userId) {
        ReservationHold hold = getCacheOrThrowIfProcessed(holdId);
        validateHold(hold, userId);
        ensureHoldNotProcessed(hold);

        stayDays(hold.checkIn(), hold.checkOut())
                .forEach(day -> countCache.decrement(hold.roomTypeId(), day, HOLD_EXPIRE_MINUTES));

        deleteAllKeys(hold);

        return new ConfirmReservationHoldResponse(holdId, hold.roomTypeId(), hold.checkIn(), hold.checkOut(), 1);
    }

    public Map<LocalDate, Long> getHoldCounts(Long roomTypeId, List<LocalDate> stayDays) {
        Map<LocalDate, Long> counts = new HashMap<>();
        for (LocalDate day : stayDays) {
            counts.put(day, countCache.get(roomTypeId, day));
        }
        return counts;
    }

    private void cleanupExistingHold(String holdId, Long roomTypeId, Long userId, LocalDate checkIn, LocalDate checkOut) {
        holdCache.find(holdId).ifPresentOrElse(
                existing -> {
                    stayDays(existing.checkIn(), existing.checkOut())
                            .forEach(day -> countCache.decrement(existing.roomTypeId(), day, HOLD_EXPIRE_MINUTES));
                    deleteAllKeys(existing);
                },
                () -> {
                    stayDays(checkIn, checkOut).forEach(day -> countCache.decrement(roomTypeId, day, HOLD_EXPIRE_MINUTES));
                    indexCache.delete(userId, roomTypeId, checkIn, checkOut);
                    throw new ReservationException(RESERVATION_HOLD_NOT_FOUND, log::info);
                }
        );
    }

    private ReservationHold getCacheOrThrowIfProcessed(String holdId) {
        return holdCache.find(holdId)
                .orElseThrow(() -> new ReservationException(RESERVATION_HOLD_ALREADY_PROCESSED, log::info));
    }

    private void validateHold(ReservationHold hold, Long userId) {
        if (!hold.userId().equals(userId)) {
            throw new ReservationException(RESERVATION_HOLD_FORBIDDEN, log::warn);
        }
        if (hold.expiredAt().isBefore(LocalDateTime.now())) {
            cleanupHoldCounts(hold);
            deleteAllKeys(hold);
            throw new ReservationException(RESERVATION_HOLD_EXPIRED, log::info);
        }
    }

    private void ensureHoldNotProcessed(ReservationHold hold) {
        List<LocalDate> stayDays = stayDays(hold.checkIn(), hold.checkOut());
        for (LocalDate day : stayDays) {
            long count = countCache.get(hold.roomTypeId(), day);
            if (count <= 0) {
                cleanupHoldCounts(hold);
                deleteAllKeys(hold);
                throw new ReservationException(RESERVATION_HOLD_ALREADY_PROCESSED, log::info);
            }
        }
    }

    private void checkIdempotencyKeyAvailable(String key, Long roomTypeId, LocalDate checkIn, LocalDate checkOut, String holdId) {

        // 1. 멱등 키가 없으면 저장하고 종료
        if (idempotencyCache.setIfAbsent(key, roomTypeId, checkIn, checkOut, holdId, HOLD_EXPIRE_MINUTES)) {
            return;
        }

        // 2. 이미 멱등키가 존재하는 경우: (멱등키 -> holdId) 매핑 정합성 확인
        String existingHoldId = idempotencyCache.find(key, roomTypeId, checkIn, checkOut)
                .orElseGet(() -> {
                    // 멱등키는 있는데 holdId가 없는 경우: 캐시 정합성 문제로 간주하고 멱등키 삭제 후 예외 발생
                    idempotencyCache.delete(key, roomTypeId, checkIn, checkOut);
                    throw new ReservationException(RESERVATION_HOLD_NOT_FOUND, log::info);
                });

        // 3. 멱등키가 가리키는 holdId가 존재하는 경우: 중복 요청 처리 (예외?)
        if (holdCache.find(existingHoldId).isPresent()) {
            throw new ReservationException(RESERVATION_HOLD_KEY_CONFLICT, log::info);
        }

        // 4. 멱등키는 있는데 holdId가 없는 경우: 캐시 정합성 문제로 간주하고 멱등키 삭제 후 예외 발생
        idempotencyCache.delete(key, roomTypeId, checkIn, checkOut);
        throw new ReservationException(RESERVATION_HOLD_NOT_FOUND, log::info);
    }
    private void cleanupHoldCounts(ReservationHold hold) {
        stayDays(hold.checkIn(), hold.checkOut())
                .forEach(day -> countCache.delete(hold.roomTypeId(), day));
    }

    private void deleteAllKeys(ReservationHold hold) {
        holdCache.delete(hold.holdId());
        indexCache.delete(hold.userId(), hold.roomTypeId(), hold.checkIn(), hold.checkOut());
        idempotencyCache.delete(hold.idempotencyKey(), hold.roomTypeId(), hold.checkIn(), hold.checkOut());
    }

    private List<LocalDate> stayDays(LocalDate checkIn, LocalDate checkOut) {
        return checkIn.datesUntil(checkOut).toList();
    }
}
