package com.f1v3.reservation.api.reservation.cache;

import com.f1v3.reservation.common.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static com.f1v3.reservation.common.redis.RedisKey.HOLD_IDEMPOTENT_FORMAT;

/**
 * 가계약의 멱등 키 캐시
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationHoldIdempotencyCache {

    private final RedisRepository redisRepository;

    public boolean setIfAbsent(
            String idempotentKey,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            String holdId,
            Duration ttl
    ) {
        String key = idempotentKey(idempotentKey, roomTypeId, checkIn, checkOut);
        Boolean result = redisRepository.setIfAbsent(key, holdId, ttl);
        return Boolean.TRUE.equals(result);
    }

    public Optional<String> find(
            String idempotentKey,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        String key = idempotentKey(idempotentKey, roomTypeId, checkIn, checkOut);
        return Optional.ofNullable(redisRepository.getValue(key));
    }

    public void save(
            String idempotentKey,
            String holdId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            Duration ttl
    ) {
        String key = idempotentKey(idempotentKey, roomTypeId, checkIn, checkOut);
        redisRepository.setValue(key, holdId, ttl);
    }

    public void delete(
            String idempotentKey,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        String key = idempotentKey(idempotentKey, roomTypeId, checkIn, checkOut);
        redisRepository.deleteValue(key);
    }

    private String idempotentKey(String key, Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        return String.format(HOLD_IDEMPOTENT_FORMAT, key, roomTypeId, checkIn, checkOut);
    }
}
