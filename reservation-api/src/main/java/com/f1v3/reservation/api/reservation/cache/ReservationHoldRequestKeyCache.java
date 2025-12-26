package com.f1v3.reservation.api.reservation.cache;

import com.f1v3.reservation.common.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static com.f1v3.reservation.common.redis.RedisKey.HOLD_REQUEST_KEY_FORMAT;

/**
 * 가계약 요청 키 캐시
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationHoldRequestKeyCache {

    private final RedisRepository redisRepository;

    public boolean setIfAbsent(
            String holdRequestKey,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            String holdId,
            Duration ttl
    ) {
        String key = holdRequestKeyCacheKey(holdRequestKey, roomTypeId, checkIn, checkOut);
        Boolean result = redisRepository.setIfAbsent(key, holdId, ttl);
        return Boolean.TRUE.equals(result);
    }

    public Optional<String> find(
            String holdRequestKey,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        String key = holdRequestKeyCacheKey(holdRequestKey, roomTypeId, checkIn, checkOut);
        return Optional.ofNullable(redisRepository.getValue(key));
    }

    public void save(
            String holdRequestKey,
            String holdId,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            Duration ttl
    ) {
        String key = holdRequestKeyCacheKey(holdRequestKey, roomTypeId, checkIn, checkOut);
        redisRepository.setValue(key, holdId, ttl);
    }

    public void delete(
            String holdRequestKey,
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        String key = holdRequestKeyCacheKey(holdRequestKey, roomTypeId, checkIn, checkOut);
        redisRepository.deleteValue(key);
    }

    private String holdRequestKeyCacheKey(String holdRequestKey, Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        return String.format(HOLD_REQUEST_KEY_FORMAT, holdRequestKey, roomTypeId, checkIn, checkOut);
    }
}
