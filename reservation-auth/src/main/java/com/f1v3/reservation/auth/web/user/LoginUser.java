package com.f1v3.reservation.auth.web.user;

import com.f1v3.reservation.common.domain.user.enums.UserRole;

/**
 * 로그인 사용자 정보를 담는 클래스.
 *
 * @author Seungjo, Jeong
 */
public record LoginUser(
        Long id,
        UserRole role
) {
}
