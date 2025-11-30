package com.f1v3.reservation.common.redis;

/**
 * Redis 키/필드 상수 모음.
 */
public final class RedisKey {

    private RedisKey() {
    }

    /**
     * 홀드 해시 키 포맷: reservation:hold:{roomTypeId}:{checkIn}:{checkOut}:{userId}
     */
    public static final String HOLD_HASH_FORMAT = "reservation:hold:%d:%s:%s:%d";

    /**
     * 만료 스캔을 위한 ZSET 인덱스 키.
     */
    public static final String HOLD_INDEX = "reservation:hold:index";

    /**
     * 해시 필드.
     */
    public static final class HashField {
        private HashField() {
        }

        public static final String QTY = "qty";
        public static final String ROOM_TYPE_ID = "roomTypeId";
        public static final String CHECK_IN = "checkIn";
        public static final String CHECK_OUT = "checkOut";
        public static final String USER_ID = "userId";
        public static final String CREATED_AT = "createdAt";
        public static final String UPDATED_AT = "updatedAt";
        public static final String EXPIRED_AT = "expiredAt";
    }
}
