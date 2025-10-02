package com.f1v3.reservation.common.encoder;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * 패스워드 인코더 클래스
 *
 * @author Seungjo, Jeong
 */
@Component
public class PasswordEncoder {

    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
