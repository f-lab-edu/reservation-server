package com.f1v3.reservation.common.domain.accommodation;

import com.f1v3.reservation.common.domain.accommodation.enums.AccommodationStatus;
import com.f1v3.reservation.common.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * 숙소 상태 변경 이력 엔티티 클래스
 *
 * @author Seungjo, Jeong
 */
@Getter
@Entity
@Table(name = "accommodation_status_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccommodationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccommodationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccommodationStatus newStatus;

    @Column(nullable = true)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime changedAt;

    @Builder
    private AccommodationStatusHistory(Accommodation accommodation, AccommodationStatus previousStatus,
                                      AccommodationStatus newStatus, String reason, User changedBy) {
        this.accommodation = accommodation;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }
}
