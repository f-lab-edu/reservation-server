package com.f1v3.reservation.common.domain.reservation;

import com.f1v3.reservation.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 예약 엔티티 (검색 기능 구현용)
 *
 * @author Seungjo, Jeong
 */
@Getter
@Entity
@Table(
        name = "reservations",
        indexes = {
                @Index(name = "idx_room_type_dates", columnList = "roomTypeId, checkIn, checkOut")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomTypeId; // 예약은 타입 단위로 진행

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Builder
    private Reservation(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        this.roomTypeId = roomTypeId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    /**
     * 주어진 날짜 범위와 예약 기간이 겹치는지 확인
     *
     * @param checkIn  체크인 날짜
     * @param checkOut 체크아웃 날짜
     * @return 겹치면 true, 아니면 false
     */
    public boolean isOverlapping(LocalDate checkIn, LocalDate checkOut) {
        return this.checkIn.isBefore(checkOut) && checkIn.isBefore(this.checkOut);
    }

    /*
    TODO: 실제 예약 기능 구현 시 확장 필요
            - 사용자 정보 연결 (userId)
            - 결제 정보 연동 (paymentId)
            - 예약 상태는 별도 확정 테이블(reservation_confirmations)에서 관리
     */
}
