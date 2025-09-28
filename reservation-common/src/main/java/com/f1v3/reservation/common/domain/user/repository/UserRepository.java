package com.f1v3.reservation.common.domain.user.repository;

import com.f1v3.reservation.common.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 회원 JPA Repository 인터페이스
 *
 * @author Seungjo, Jeong
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
