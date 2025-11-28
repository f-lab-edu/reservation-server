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

    @Column(nullable = false)
    private Long userId;

    // todo: 상태 값 (전환 가능한 상태 체크 필요) FSM -> if-else의 문제점 해결
    @Builder
    private Reservation(Long userId, Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        this.userId = userId;
        this.roomTypeId = roomTypeId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    /*
    TODO: 실제 예약 기능 구현 시 확장 필요
            - 사용자 정보 연결 (userId)
            - 결제 정보 연동 (paymentId)
            - 예약 상태는 별도 확정 테이블(reservation_confirmations)에서 관리
     */
}
