package com.f1v3.reservation.common.domain.room;

import com.f1v3.reservation.common.domain.accommodation.Accommodation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * 객실 타입 엔티티
 * 예약은 이 RoomType 단위로 이루어지며, 체크인 시 실제 RoomUnit(호실)을 배정받음
 *
 * @author Seungjo, Jeong
 */
@Getter
@Entity
@Table(name = "room_types")
@SQLDelete(sql = "UPDATE room_types SET isActive = true WHERE id = ?")
@SQLRestriction("isActive = true")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer standardCapacity;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private Integer totalRoomCount;

    @Column(columnDefinition = "TEXT")
    private String thumbnail;

    @Column(nullable = false)
    private boolean isActive;

    @Builder
    private RoomType(Accommodation accommodation, String name, String description,
                     Integer standardCapacity, Integer maxCapacity, BigDecimal basePrice,
                     Integer totalRoomCount, String thumbnail) {
        this.accommodation = accommodation;
        this.name = name;
        this.description = description;
        this.standardCapacity = standardCapacity;
        this.maxCapacity = maxCapacity;
        this.basePrice = basePrice;
        this.totalRoomCount = totalRoomCount;
        this.thumbnail = thumbnail;
        this.isActive = true;
    }

    public void updateDetails(String name, String description, int standardCapacity, int maxCapacity,
                              BigDecimal basePrice, int totalRoomCount, String thumbnail) {
        this.name = name;
        this.description = description;
        this.standardCapacity = standardCapacity;
        this.maxCapacity = maxCapacity;
        this.basePrice = basePrice;
        this.totalRoomCount = totalRoomCount;
        this.thumbnail = thumbnail;
    }
}
