package com.f1v3.reservation.api.phoneverification.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 로그를 통해 SMS 발송을 시뮬레이션하는 SMS 제공자 클래스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Component
@Profile("!prod")
public class LogSmsProvider implements SmsProvider {

    @Override
    public void send(String phoneNumber, String message) {
        log.info("[SMS 발송] 전화번호: {}, 메시지: {}", phoneNumber, message);
    }
}
