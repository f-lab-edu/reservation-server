package com.f1v3.reservation.api.phoneverification.sms;

/**
 * SMS 발송을 위한 인터페이스.
 *
 * @author Seungjo, Jeong
 */
public interface SmsProvider {
    void send(String phoneNumber, String message);
}
