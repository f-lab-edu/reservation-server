package com.f1v3.reservation.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.auditing.config.AuditingConfiguration;

import java.time.LocalDateTime;

/**
 * 생성 및 수정 시간을 자동으로 관리하는 베이스 엔티티 클래스.
 *
 * @author Seungjo, Jeong
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingConfiguration.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
