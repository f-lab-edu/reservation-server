package com.f1v3.reservation.api.reservation.cache;

import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.reservation.ReservationHold;
import com.f1v3.reservation.common.redis.RedisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

import static com.f1v3.reservation.common.redis.RedisKey.HOLD_KEY_FORMAT;

/**
 * 가계약 캐시
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationHoldCache {

    private final RedisRepository redisRepository;
    private final ObjectMapper objectMapper;

    public Optional<ReservationHold> find(String holdId) {
        String value = redisRepository.getValue(holdKey(holdId));

        if (value == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(value, ReservationHold.class));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize reservation hold. holdId={}", holdId, e);
            throw new ReservationException(ErrorCode.SERVER_ERROR, log::error);
        }
    }

    public void save(ReservationHold hold, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(hold);
            String key = holdKey(hold.holdId());
            redisRepository.setValue(key, json, ttl);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize reservation hold. holdId={}", hold.holdId(), e);
            throw new ReservationException(ErrorCode.SERVER_ERROR, log::error);
        }
    }

    public void delete(String holdId) {
        String key = holdKey(holdId);
        redisRepository.deleteValue(key);
    }

    private String holdKey(String holdId) {
        return String.format(HOLD_KEY_FORMAT, holdId);
    }

}
