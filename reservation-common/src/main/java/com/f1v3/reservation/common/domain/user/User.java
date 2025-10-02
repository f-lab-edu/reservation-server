package com.f1v3.reservation.common.domain.user;

import com.f1v3.reservation.common.domain.BaseEntity;
import com.f1v3.reservation.common.domain.user.enums.Gender;
import com.f1v3.reservation.common.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 회원 엔티티 클래스
 *
 * @author Seungjo, Jeong
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_password", nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Builder
    private User(String password, String email, String nickname, String phoneNumber, LocalDate birth,
                 Gender gender, UserRole role) {
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.birthDate = birth;
        this.gender = gender;
        this.role = role;
    }

    public static User.UserBuilder createUser() {
        return User.builder().role(UserRole.USER);
    }

    public static User.UserBuilder createSupplier() {
        return User.builder().role(UserRole.SUPPLIER);
    }

    public static User.UserBuilder createAdmin() {
        return User.builder().role(UserRole.ADMIN);
    }
}
