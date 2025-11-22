package com.f1v3.reservation.common.domain.reservation;

import com.f1v3.reservation.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 임시 예약 엔티티
 */
@Getter
@Entity
@Table(name = "reservation_holds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationHold extends BaseEntity {

    @Id
    @Column(length = 36)
    private String holdId;

    // userId-roomTypeId-checkIn-checkOut -> Hash Key in Redis
    // value -> stock

    @Column(nullable = false)
    private Long userId; // 예약자 ID

    @Column(nullable = false)
    private Long roomTypeId;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Builder
    private ReservationHold(String holdId, Long roomTypeId, Long userId,
                            LocalDate checkIn, LocalDate checkOut, Integer capacity,
                            LocalDateTime expiredAt) {
        this.holdId = holdId;
        this.roomTypeId = roomTypeId;
        this.userId = userId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.capacity = capacity;
        this.expiredAt = expiredAt;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiredAt.isBefore(now);
    }

    public boolean isOwner(Long id) {
        return this.userId.equals(id);
    }
}
