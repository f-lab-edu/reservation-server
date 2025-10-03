package com.f1v3.reservation.api.user;

import com.f1v3.reservation.api.phoneverification.PhoneVerificationService;
import com.f1v3.reservation.api.user.dto.SignupUserRequest;
import com.f1v3.reservation.api.user.dto.SignupUserResponse;
import com.f1v3.reservation.common.domain.user.User;
import com.f1v3.reservation.common.domain.user.enums.Gender;
import com.f1v3.reservation.common.domain.user.repository.UserRepository;
import com.f1v3.reservation.common.encoder.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 서비스 클래스
 *
 * @author Seungjo, Jeong
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserTermAgreementService userTermAgreementService;
    private final PhoneVerificationService phoneVerificationService;
    private final UserValidationService userValidationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupUserResponse signup(SignupUserRequest request) {

        phoneVerificationService.checkVerified(request.phoneNumber());
        userValidationService.checkPhoneNumberExists(request.phoneNumber());
        userValidationService.checkEmailExists(request.email());

        String encryptedPassword = passwordEncoder.encode(request.password());

        User newUser = User.createUser()
                .email(request.email())
                .password(encryptedPassword)
                .nickname(request.nickname())
                .phoneNumber(request.phoneNumber())
                .birth(request.birth())
                .gender(Gender.getGender(request.gender()))
                .build();

        User savedUser = userRepository.save(newUser);
        userTermAgreementService.createAgreements(savedUser, request.agreedTerms());

        return new SignupUserResponse(savedUser.getId());
    }
}
