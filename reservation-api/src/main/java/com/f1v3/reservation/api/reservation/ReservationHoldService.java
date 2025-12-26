package com.f1v3.reservation.api.reservation;

import com.f1v3.reservation.api.reservation.cache.ReservationHoldCache;
import com.f1v3.reservation.api.reservation.cache.ReservationHoldCountCache;
import com.f1v3.reservation.api.reservation.cache.ReservationHoldIndexCache;
import com.f1v3.reservation.api.reservation.cache.ReservationHoldRequestKeyCache;
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

    private static final Duration HOLD_EXPIRE_MINUTES = Duration.ofMinutes(10);

    private final ReservationHoldCache holdCache;
    private final ReservationHoldIndexCache indexCache;
    private final ReservationHoldRequestKeyCache holdRequestKeyCache;
    private final ReservationHoldCountCache countCache;

    /**
     * 가계약 및 관련 캐시 생성
     * 1. 기존 가계약 정리 (같은 사용자가 동일 객실타입/기간으로 가계약을 여러개 생성하는 경우 방지)
     * 2. 가계약 생성
     * 3. 요청 키 확인 (중복 요청 방지)
     * 4. 가계약 저장 및 인덱스/요청키/카운트 캐시 생성
     *
     * @return 생성된 가계약 ID 및 만료 시각
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
                request.holdRequestKey(),
                now,
                expiredAt
        );

        // 3. 요청 키 확인 (중복 요청 방지)
        checkHoldRequestKeyAvailable(request.holdRequestKey(), roomType.getId(), checkIn, checkOut, holdId);

        holdCache.save(hold, HOLD_EXPIRE_MINUTES);
        indexCache.save(userId, roomType.getId(), checkIn, checkOut, holdId, HOLD_EXPIRE_MINUTES);
        holdRequestKeyCache.save(request.holdRequestKey(), holdId, roomType.getId(), checkIn, checkOut, HOLD_EXPIRE_MINUTES);
        stayDays.forEach(day -> countCache.increment(roomType.getId(), day, HOLD_EXPIRE_MINUTES));

        return new ReservationHoldResponse(holdId, expiredAt);
    }

    public Map<LocalDate, Long> getHoldCounts(Long roomTypeId, List<LocalDate> stayDays) {
        return countCache.getCounts(roomTypeId, stayDays);
    }

    /**
     * 락 만료 등으로 가계약을 무효화해야 할 때, holdId 기준으로 안전하게 정리한다.
     */
    public void cleanup(String holdId) {
        ReservationHold hold = holdCache.find(holdId).orElse(null);
        if (hold == null) {
            return;
        }

        cleanupHoldCounts(hold);
        holdCache.delete(hold.holdId());
        deleteIndexIfMatch(hold);
        deleteRequestKeyIfMatch(hold);
    }

    private void cleanupHoldCounts(ReservationHold hold) {
        stayDays(hold.checkIn(), hold.checkOut())
                .forEach(day -> countCache.decrement(hold.roomTypeId(), day, HOLD_EXPIRE_MINUTES));
    }

    private void deleteIndexIfMatch(ReservationHold hold) {
        indexCache.findHoldId(hold.userId(), hold.roomTypeId(), hold.checkIn(), hold.checkOut())
                .filter(hold.holdId()::equals)
                .ifPresent(existingHoldId -> indexCache.delete(
                        hold.userId(),
                        hold.roomTypeId(),
                        hold.checkIn(),
                        hold.checkOut()
                ));
    }

    private void deleteRequestKeyIfMatch(ReservationHold hold) {
        holdRequestKeyCache.find(hold.holdRequestKey(), hold.roomTypeId(), hold.checkIn(), hold.checkOut())
                .filter(hold.holdId()::equals)
                .ifPresent(existingHoldId -> holdRequestKeyCache.delete(
                        hold.holdRequestKey(),
                        hold.roomTypeId(),
                        hold.checkIn(),
                        hold.checkOut()
                ));
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

    private void checkHoldRequestKeyAvailable(
            String holdRequestKey,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            String holdId
    ) {

        // 1. 요청 키가 없으면 저장하고 종료
        if (holdRequestKeyCache.setIfAbsent(holdRequestKey, roomTypeId, checkIn, checkOut, holdId, HOLD_EXPIRE_MINUTES)) {
            return;
        }

        // 2. 이미 요청 키가 존재하는 경우: (요청 키 -> holdId) 매핑 정합성 확인
        String existingHoldId = holdRequestKeyCache.find(holdRequestKey, roomTypeId, checkIn, checkOut)
                .orElseGet(() -> {
                    // 요청 키는 있는데 holdId가 없는 경우: 캐시 정합성 문제로 간주하고 요청 키 삭제 후 예외 발생
                    holdRequestKeyCache.delete(holdRequestKey, roomTypeId, checkIn, checkOut);
                    throw new ReservationException(RESERVATION_HOLD_NOT_FOUND, log::info);
                });

        // 3. 요청 키가 가리키는 holdId가 존재하는 경우: 중복 요청 처리 (409 예외)
        if (holdCache.find(existingHoldId).isPresent()) {
            throw new ReservationException(RESERVATION_HOLD_REQUEST_KEY_CONFLICT, log::info);
        }

        // 4. 요청 키는 있는데 holdId가 없는 경우: 캐시 정합성 문제로 간주하고 요청 키 삭제 후 예외 발생
        holdRequestKeyCache.delete(holdRequestKey, roomTypeId, checkIn, checkOut);
        throw new ReservationException(RESERVATION_HOLD_NOT_FOUND, log::info);
    }

    private List<LocalDate> stayDays(LocalDate checkIn, LocalDate checkOut) {
        return checkIn.datesUntil(checkOut.plusDays(1)).toList();
    }

    private void deleteAllKeys(ReservationHold hold) {
        holdCache.delete(hold.holdId());
        indexCache.delete(hold.userId(), hold.roomTypeId(), hold.checkIn(), hold.checkOut());
        holdRequestKeyCache.delete(hold.holdRequestKey(), hold.roomTypeId(), hold.checkIn(), hold.checkOut());
    }
}
