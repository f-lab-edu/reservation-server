package com.f1v3.reservation.api.phoneverification.sms;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 실제 SMS 발송 로직을 구현하는 SMS 제공자 클래스 (미구현 상태)
 *
 * @author Seungjo, Jeong
 */
@Component
@Profile("prod")
public class ProdSmsProvider implements SmsProvider {

    @Override
    public void send(String phoneNumber, String message) {
        // 실제 SMS 발송 로직을 여기에 구현합니다.
    }
}
