package com.f1v3.reservation.common.domain.phoneverification.repository;

import com.f1v3.reservation.common.domain.phoneverification.PhoneVerification;
import com.f1v3.reservation.common.domain.phoneverification.QPhoneVerification;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 핸드폰 인증 Querydsl Repository 구현체
 *
 * @author Seungjo, Jeong
 */
@RequiredArgsConstructor
public class PhoneVerificationRepositoryImpl implements PhoneVerificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QPhoneVerification phoneVerification = QPhoneVerification.phoneVerification;

    @Override
    public Optional<PhoneVerification> findLatestByPhoneNumber(String phoneNumber) {

        return Optional.ofNullable(queryFactory
                .select(phoneVerification)
                .from(phoneVerification)
                .where(phoneVerification.phoneNumber.eq(phoneNumber)
                        .and(phoneVerification.isVerified.isFalse())
                        .and(phoneVerification.expiredAt.gt(LocalDateTime.now())))
                .orderBy(phoneVerification.id.desc())
                .fetchFirst());
    }
}
