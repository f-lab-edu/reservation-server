package com.f1v3.reservation.api.reservation.cache;

import com.f1v3.reservation.common.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static com.f1v3.reservation.common.redis.RedisKey.HOLD_IDX_FORMAT;

/**
 * 가계약 인덱스 캐시
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationHoldIndexCache {

    private final RedisRepository redisRepository;

    public Optional<String> findHoldId(
            Long userId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        String key = idxKey(userId, roomTypeId, checkIn, checkOut);
        return Optional.ofNullable(redisRepository.getValue(key));
    }

    public void save(
            Long userId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            String holdId,
            Duration ttl
    ) {
        String key = idxKey(userId, roomTypeId, checkIn, checkOut);
        redisRepository.setValue(key, holdId, ttl);
    }

    public void delete(
            Long userId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        String key = idxKey(userId, roomTypeId, checkIn, checkOut);
        redisRepository.deleteValue(key);
    }

    private String idxKey(Long userId, Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        return String.format(HOLD_IDX_FORMAT, userId, roomTypeId, checkIn, checkOut);
    }
}
