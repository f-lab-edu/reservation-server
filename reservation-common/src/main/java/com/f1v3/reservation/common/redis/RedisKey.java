package com.f1v3.reservation.common.redis;

/**
 * Redis 키/필드 상수 모음.
 */
public final class RedisKey {

    private RedisKey() {
    }

    /**
     * 가계약 키 포맷
     */
    public static final String HOLD_KEY_FORMAT = "reservation:hold:%s";
    public static final String HOLD_IDX_FORMAT = "reservation:hold:idx:%d:%d:%s:%s";
    public static final String HOLD_IDEMPOTENT_FORMAT = "reservation:hold:idempotent:%s:%d:%s:%s";
    public static final String HOLD_COUNT_FORMAT = "reservation:hold:count:%d:%s";

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
        public static final String EXPIRED_AT = "expiredAt";
    }
}
