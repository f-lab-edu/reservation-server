package com.f1v3.reservation.api.reservation;

import com.f1v3.reservation.api.reservation.dto.CreateReservationHoldRequest;
import com.f1v3.reservation.api.reservation.dto.ReservationHoldResponse;
import com.f1v3.reservation.common.domain.room.RoomType;
import com.f1v3.reservation.common.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.f1v3.reservation.common.redis.RedisKey.HOLD_HASH_FORMAT;
import static com.f1v3.reservation.common.redis.RedisKey.HOLD_INDEX;
import static com.f1v3.reservation.common.redis.RedisKey.HashField.*;

/**
 * 임시 예약 서비스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationHoldService {

    private static final Duration HOLD_EXPIRE_WINDOW = Duration.ofMinutes(10);

    private final RedisRepository redisRepository;

    /**
     * 임시 예약 생성 + Redis Hash 저장 (TTL 미사용, 만료 배치 전제)
     */
    public ReservationHoldResponse createHold(
            Long userId,
            RoomType roomType,
            CreateReservationHoldRequest request
    ) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plus(HOLD_EXPIRE_WINDOW);
        double expiredAtMillis = expiredAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        String key = redisKey(roomType.getId(), request.checkIn(), request.checkOut(), userId);

        try {
            redisRepository.execute(new SessionCallback<List<Object>>() {
                @SuppressWarnings({"unchecked", "rawtypes"})
                @Override
                public List<Object> execute(RedisOperations operations) throws DataAccessException {
                    operations.multi(); // tx start
                    operations.opsForHash().increment(key, QTY, 1);
                    operations.opsForHash().putIfAbsent(key, ROOM_TYPE_ID, roomType.getId().toString());
                    operations.opsForHash().putIfAbsent(key, CHECK_IN, request.checkIn().toString());
                    operations.opsForHash().putIfAbsent(key, CHECK_OUT, request.checkOut().toString());
                    operations.opsForHash().putIfAbsent(key, USER_ID, userId.toString());
                    operations.opsForHash().putIfAbsent(key, CREATED_AT, now.toString());
                    operations.opsForHash().put(key, UPDATED_AT, now.toString());
                    operations.opsForHash().put(key, EXPIRED_AT, expiredAt.toString());
                    operations.opsForZSet().add(HOLD_INDEX, key, expiredAtMillis);
                    return operations.exec(); // tx commit
                }
            });
        } catch (DataAccessException e) {
            log.error("Failed to create reservation hold in Redis. roomTypeId={}, checkIn={}, checkOut={}, userId={}",
                    roomType.getId(), request.checkIn(), request.checkOut(), userId, e);
            throw e; // fixme: 예외 어떻게 처리할지 고민
        }

        return new ReservationHoldResponse(key, expiredAt);
    }

    private String redisKey(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, Long userId) {
        return String.format(HOLD_HASH_FORMAT, roomTypeId, checkIn, checkOut, userId);
    }

}
