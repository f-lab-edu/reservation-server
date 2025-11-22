package com.f1v3.reservation.common.domain.room;

import com.f1v3.reservation.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 객실 타입 일자별 재고
 *
 * @author Seungjo, Jeong
 */
@Getter
@Entity
@Table(name = "room_type_stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomTypeStock extends BaseEntity {

    @EmbeddedId
    private RoomTypeStockPk roomTypeStockPk;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Builder
    private RoomTypeStock(RoomTypeStockPk roomTypeStockPk, Integer totalQuantity, Integer availableQuantity) {
        this.roomTypeStockPk = roomTypeStockPk;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
    }

    public boolean hasAvailable() {
        return availableQuantity > 0;
    }

    public void decrease() {
        if (availableQuantity <= 0) {
            throw new IllegalStateException("객실 재고가 부족합니다.");
        }

        this.availableQuantity -= 1;
    }

    public void increase() {
        if (availableQuantity < totalQuantity) {
            this.availableQuantity += 1;
        }
    }

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class RoomTypeStockPk implements Serializable {

        @Column(nullable = false)
        private Long roomTypeId;

        @Column(nullable = false)
        private LocalDate targetDate;
    }
}
