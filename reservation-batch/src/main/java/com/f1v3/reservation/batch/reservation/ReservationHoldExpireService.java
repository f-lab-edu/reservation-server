package com.f1v3.reservation.batch.reservation;

import com.f1v3.reservation.common.domain.room.RoomTypeStock;
import com.f1v3.reservation.common.domain.room.repository.RoomTypeStockRepository;
import com.f1v3.reservation.common.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.f1v3.reservation.common.redis.RedisKey.HOLD_INDEX;
import static com.f1v3.reservation.common.redis.RedisKey.HashField.*;

/**
 * 임시 예약 만료 복원 서비스 (재고 복원)
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationHoldExpireService {

    private static final int DEFAULT_SCAN_SIZE = 200;

    private final RedisRepository redisRepository;
    private final RoomTypeStockRepository roomTypeStockRepository;

    /**
     * 만료된 임시 예약 복원 처리
     * 1. 만료된 임시 예약 키 스캔
     * 2. Redis Hash에서 임시 예약 정보 조회
     * 3. 재고 복원 및 Redis 정리
     */
    @Transactional
    public void restoreExpiredHolds() {
        long nowMillis = System.currentTimeMillis();
        Set<String> candidates = redisRepository.zRangeByScore(HOLD_INDEX, 0, nowMillis, 0, DEFAULT_SCAN_SIZE);
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        for (String key : candidates) {
            try {
                processExpiredHold(key, nowMillis);
            } catch (Exception e) {
                log.warn("Failed to process expired hold. key={}", key, e);
            }
        }
    }

    private void processExpiredHold(String key, long nowMillis) {
        List<Object> values = redisRepository.getHashValues(key, List.of(QTY, ROOM_TYPE_ID, CHECK_IN, CHECK_OUT, EXPIRED_AT));
        if (values == null || values.contains(null)) {
            cleanupRedis(key);
            return;
        }

        int qty = parseInt(values.get(0));
        Long roomTypeId = parseLong(values.get(1));
        LocalDate checkIn = LocalDate.parse(values.get(2).toString());
        LocalDate checkOut = LocalDate.parse(values.get(3).toString());
        LocalDateTime expiredAt = LocalDateTime.parse(values.get(4).toString());

        long expireMillis = expiredAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        if (expireMillis > nowMillis) {
            // 스코어가 늦춰졌다면 인덱스만 최신으로 덮어쓴다.
            redisRepository.zAdd(HOLD_INDEX, key, expireMillis);
            return;
        }

        List<LocalDate> stayDays = Stream.iterate(checkIn, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(checkIn, checkOut))
                .toList();

        List<RoomTypeStock> stocks = roomTypeStockRepository.findAllByRoomTypeIdAndTargetDates(roomTypeId, stayDays);
        if (stocks.size() != stayDays.size()) {
            log.warn("Stock missing while restoring hold. key={}, roomTypeId={}, stayDays={}", key, roomTypeId, stayDays);
            cleanupRedis(key);
            return;
        }

        stocks.forEach(stock -> {
            for (int i = 0; i < qty; i++) {
                stock.increase();
            }
        });
        roomTypeStockRepository.saveAll(stocks);

        cleanupRedis(key);
    }

    private void cleanupRedis(String key) {
        redisRepository.execute(new SessionCallback<List<Object>>() {
            @SuppressWarnings({"unchecked", "rawtypes"})
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.delete(key);
                operations.opsForZSet().remove(HOLD_INDEX, key);
                return operations.exec();
            }
        });
    }

    private int parseInt(Object value) {
        return value == null ? 0 : Integer.parseInt(value.toString());
    }

    private Long parseLong(Object value) {
        return value == null ? null : Long.parseLong(value.toString());
    }
}
