package com.f1v3.reservation.api.reservation;

import com.f1v3.reservation.api.reservation.dto.ConfirmReservationHoldResponse;
import com.f1v3.reservation.api.reservation.dto.CreateReservationHoldRequest;
import com.f1v3.reservation.api.reservation.dto.ReservationHoldResponse;
import com.f1v3.reservation.common.api.error.ReservationException;
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

import static com.f1v3.reservation.common.api.error.ErrorCode.*;
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

    public ConfirmReservationHoldResponse confirmReservationHold(String holdKey, Long userId) {
        // 1. Redis에서 홀드 정보 조회
        List<Object> values = redisRepository.getHashValues(holdKey, List.of(QTY, ROOM_TYPE_ID, CHECK_IN, CHECK_OUT, EXPIRED_AT, USER_ID));

        if (values == null || values.contains(null)) {
            throw new ReservationException(RESERVATION_HOLD_NOT_FOUND, log::info);
        }

        // 2. 사용자 검증 (홀드 생성한 사용자와 동일한지)
        Long holdUserId = Long.parseLong(values.get(5).toString());
        if (!holdUserId.equals(userId)) {
            throw new ReservationException(RESERVATION_HOLD_FORBIDDEN, log::warn);
        }

        // 3. 홀드 정보 파싱
        int qty = Integer.parseInt(values.get(0).toString());
        Long roomTypeId = Long.parseLong(values.get(1).toString());
        LocalDate checkIn = LocalDate.parse(values.get(2).toString());
        LocalDate checkOut = LocalDate.parse(values.get(3).toString());
        LocalDateTime expiredAt = LocalDateTime.parse(values.get(4).toString());

        LocalDateTime now = LocalDateTime.now();

        // 4. 만료 검증
        if (expiredAt.isBefore(now)) {
            throw new ReservationException(RESERVATION_HOLD_EXPIRED, log::info);
        }

        // 5. Redis에서 홀드 정보 삭제
        redisRepository.execute(new SessionCallback<List<Object>>() {
            @SuppressWarnings({"unchecked"})
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                // 임시 예약 정보 삭제
                operations.delete(holdKey);

                // 인덱스(ZSet)에 저장된 만료 정보 삭제
                operations.opsForZSet().remove(HOLD_INDEX, holdKey);
                return operations.exec();
            }
        });

        return new ConfirmReservationHoldResponse(holdKey, roomTypeId, checkIn, checkOut, qty);
    }

    private String redisKey(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, Long userId) {
        return String.format(HOLD_HASH_FORMAT, roomTypeId, checkIn, checkOut, userId);
    }

}
