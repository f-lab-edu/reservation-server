package com.f1v3.reservation.api.reservation.cache;

import com.f1v3.reservation.common.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 가계약 보유 카운트 캐시
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationHoldCountCache {

    private final RedisRepository redisRepository;

    public long increment(Long roomTypeId, LocalDate day, Duration ttl) {
        String key = countKey(roomTypeId, day);
        Long count = redisRepository.increment(key, 1);
        redisRepository.expire(key, ttl);

        return count == null ? 0L : count;
    }

    public long decrement(Long roomTypeId, LocalDate day, Duration ttl) {
        String key = countKey(roomTypeId, day);
        Long count = redisRepository.decrement(key, 1);

        if (count != null && count <= 0) {
            redisRepository.deleteValue(key);
            return 0L;
        }

        redisRepository.expire(key, ttl);

        return count == null ? 0L : count;
    }

    public long get(Long roomTypeId, LocalDate day) {
        String key = countKey(roomTypeId, day);
        String value = redisRepository.getValue(key);

        if (value == null) {
            return 0L;
        }

        return Long.parseLong(value);
    }

    public void delete(Long roomTypeId, LocalDate day) {
        String key = countKey(roomTypeId, day);
        redisRepository.deleteValue(key);
    }

    private String countKey(Long roomTypeId, LocalDate day) {
        return String.format(com.f1v3.reservation.common.redis.RedisKey.HOLD_COUNT_FORMAT, roomTypeId, day);
    }

    public Map<LocalDate, Long> getCounts(Long roomTypeId, List<LocalDate> stayDays) {
        List<String> keys = stayDays.stream()
                .map(day -> countKey(roomTypeId, day))
                .toList();
        List<String> values = redisRepository.getMultiValues(keys);

        Map<LocalDate, Long> counts = HashMap.newHashMap(stayDays.size());
        for (int i = 0; i < stayDays.size(); i++) {
            String value = values.get(i);
            long count = value == null ? 0L : Long.parseLong(value);
            counts.put(stayDays.get(i), count);
        }
        return counts;
    }
}
