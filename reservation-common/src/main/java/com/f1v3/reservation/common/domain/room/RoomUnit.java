package com.f1v3.reservation.common.domain.room;

import com.f1v3.reservation.common.domain.room.enums.RoomUnitStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 실제 객실(호실) 엔티티 (예: 101호, 102호 등)
 * 체크인 시 예약에 대해 실제 Room을 배정함
 *
 * @author Seungjo, Jeong
 */
@Getter
@Entity
@Table(name = "room_units")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false, length = 20)
    private String roomNumber; // 호실 번호 (예: "101", "202")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomUnitStatus status;

    @Column(nullable = true, length = 255)
    private String notes;

    @Builder
    private RoomUnit(RoomType roomType, String roomNumber) {
        this.roomType = roomType;
        this.roomNumber = roomNumber;
        this.status = RoomUnitStatus.AVAILABLE;
    }

    public void updateStatus(RoomUnitStatus status) {
        this.status = status;
    }

    public void updateNotes(String notes) {
        this.notes = notes;
    }
}
