package com.f1v3.reservation.admin;

import com.f1v3.reservation.common.domain.user.User;
import com.f1v3.reservation.common.domain.user.enums.Gender;
import com.f1v3.reservation.common.domain.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 관리자 API 호출을 위해 관리자 계정 생성
 *
 * @author Seungjo, Jeong
 */
@Component
@RequiredArgsConstructor
public class AdminInit {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @PostConstruct
    public void init() {

        userRepository.deleteAll();

        String password = encoder.encode("admin1234!");
        User admin = User.createAdmin()
                .email("admin@admin.com")
                .password(password)
                .nickname("관리자1")
                .phoneNumber("010-1234-5678")
                .birth(LocalDate.of(1999, 5, 13))
                .gender(Gender.M)
                .build();

        userRepository.save(admin);
    }
}
