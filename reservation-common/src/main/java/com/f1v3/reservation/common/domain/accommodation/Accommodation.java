package com.f1v3.reservation.common.domain.accommodation;

import com.f1v3.reservation.common.domain.BaseEntity;
import com.f1v3.reservation.common.domain.accommodation.enums.AccommodationStatus;
import com.f1v3.reservation.common.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 숙소 엔티티 클래스
 *
 * @author Seungjo, Jeong
 */
@Getter
@Entity
@Table(name = "accommodations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Accommodation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private User supplier;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String contactNumber;

    @Column(nullable = false)
    private String thumbnail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccommodationStatus status;

    @Column(nullable = false)
    private boolean isVisible;

    @Builder
    private Accommodation(User supplier, String name, String description, String address, String contactNumber, String thumbnail) {
        this.supplier = supplier;
        this.name = name;
        this.description = description;
        this.address = address;
        this.contactNumber = contactNumber;
        this.thumbnail = thumbnail;
        this.status = AccommodationStatus.PENDING;
        this.isVisible = false; // 기본값 false, 관리자 승인 후 true
    }

    public void approve() {
        this.status = AccommodationStatus.APPROVED;
        this.isVisible = true;
    }

    public void update(String name, String description, String address, String contactNumber, String thumbnail) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.contactNumber = contactNumber;
        this.thumbnail = thumbnail;
    }
}
