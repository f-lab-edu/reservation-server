package com.f1v3.reservation.common.domain.phoneverification.repository;

import com.f1v3.reservation.common.domain.phoneverification.PhoneVerification;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * 휴대폰 인증 커스텀 리포지토리 인터페이스
 *
 * @author Seungjo, Jeong
 */
@NoRepositoryBean
public interface PhoneVerificationRepositoryCustom {

    Optional<PhoneVerification> findLatestByPhoneNumber(String phoneNumber);

    Optional<PhoneVerification> findLatestVerifiedByPhoneNumber(String phoneNumber);
}
