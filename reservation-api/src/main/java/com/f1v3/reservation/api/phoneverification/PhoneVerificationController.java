package com.f1v3.reservation.api.phoneverification;

import com.f1v3.reservation.api.phoneverification.dto.SendPhoneVerificationRequest;
import com.f1v3.reservation.api.phoneverification.dto.SendPhoneVerificationResponse;
import com.f1v3.reservation.api.phoneverification.dto.VerifyPhoneVerificationRequest;
import com.f1v3.reservation.common.api.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SendPhoneVerificationResponse> sendVerifyCode(@Valid @RequestBody SendPhoneVerificationRequest request) {
        SendPhoneVerificationResponse response = phoneVerificationService.sendVerifyCode(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.CREATED)
    public void verifyCode(@Valid @RequestBody VerifyPhoneVerificationRequest request) {
        phoneVerificationService.incrementAttempt(request.phoneNumber());
        phoneVerificationService.verifyCode(request);
    }
}
