package com.f1v3.reservation.common.domain.user.repository;

import com.f1v3.reservation.common.domain.user.UserTermAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 회원 약관 동의 목록 JPA Repository 인터페이스
 *
 * @author Seungjo, Jeong
 */
public interface UserTermAgreementRepository extends JpaRepository<UserTermAgreement, Long> {
}
