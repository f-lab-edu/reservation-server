package com.f1v3.reservation.common.domain.user.repository;

import com.f1v3.reservation.common.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 회원 JPA Repository 인터페이스
 *
 * @author Seungjo, Jeong
 */
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);
}
