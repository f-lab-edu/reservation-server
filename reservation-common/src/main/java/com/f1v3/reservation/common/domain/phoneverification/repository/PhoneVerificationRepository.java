package com.f1v3.reservation.common.domain.phoneverification.repository;

import com.f1v3.reservation.common.domain.phoneverification.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 휴대폰 인증 엔티티의 JPA Repository 인터페이스
 *
 * @author Seungjo, Jeong
 */
public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {

    Optional<PhoneVerification> findByPhoneNumber(String phoneNumber);
}
