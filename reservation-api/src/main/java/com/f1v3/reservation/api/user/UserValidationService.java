package com.f1v3.reservation.api.user;

import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.f1v3.reservation.common.api.error.ErrorCode.USER_EMAIL_ALREADY_EXISTS;
import static com.f1v3.reservation.common.api.error.ErrorCode.USER_PHONE_ALREADY_EXISTS;

/**
 * 회원 검증 서비스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final UserRepository userRepository;

    public void checkPhoneNumberExists(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ReservationException(USER_PHONE_ALREADY_EXISTS, log::info);
        }
    }

    public void checkEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ReservationException(USER_EMAIL_ALREADY_EXISTS, log::info);
        }
    }
}
