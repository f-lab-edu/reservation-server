package com.f1v3.reservation.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

    public void setHashValue(String key, String hashKey, String value) {
        // todo: hashKey는 어떤 형식으로 관리할지 고민 필요
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public List<Object> getHashValues(String key, Collection<Object> hashKeys) {
        return redisTemplate.opsForHash().multiGet(key, hashKeys);
    }

    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    public Set<String> zRangeByScore(String key, double minScore, double maxScore, long offset, long count) {
        return redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore, offset, count);
    }

    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    public <T> T execute(SessionCallback<T> callback) throws DataAccessException {
        return redisTemplate.execute(callback);
    }

    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }
}
