package com.f1v3.reservation.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * Redis 레포지토리 클래스
 *
 * @author Seungjo, Jeong
 */
@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void setValue(String key, String value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
