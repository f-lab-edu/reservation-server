package com.f1v3.reservation.api.phoneverification;

import com.f1v3.reservation.api.phoneverification.dto.SendPhoneVerificationRequest;
import com.f1v3.reservation.api.phoneverification.dto.SendPhoneVerificationResponse;
import com.f1v3.reservation.api.phoneverification.dto.VerifyPhoneVerificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 핸드폰 인증 컨트롤러 클래스
 *
 * @author Seungjo, Jeong
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/phone-verifications")
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    @PostMapping("/send")
    public ResponseEntity<SendPhoneVerificationResponse> sendVerifyCode(@Valid @RequestBody SendPhoneVerificationRequest request) {

        SendPhoneVerificationResponse response = phoneVerificationService.sendVerifyCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyCode(@Valid @RequestBody VerifyPhoneVerificationRequest request) {
        phoneVerificationService.verifyCode(request);
        return ResponseEntity.ok().build();
    }
}
