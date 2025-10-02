package com.f1v3.reservation.api.term;

import com.f1v3.reservation.api.term.dto.TermResponse;
import com.f1v3.reservation.api.user.dto.SignupUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 회원가입 약관 동의 검증 서비스
 *
 * @author Seungjo, Jeong
 */
@Service
@RequiredArgsConstructor
public class TermValidationService {

    private final TermService termService;

    public void validateRequiredTermsAgreed(Set<SignupUserRequest.SignupTermRequest> termRequests) {
        Set<Long> requiredTermIds = termService.getActiveTerms().stream()
                .filter(TermResponse::isRequired)
                .map(TermResponse::termId)
                .collect(Collectors.toSet());

        Set<Long> agreedTermIds = termRequests.stream()
                .map(SignupUserRequest.SignupTermRequest::termId)
                .collect(Collectors.toSet());

        requiredTermIds.removeAll(agreedTermIds);

        if (!requiredTermIds.isEmpty()) {
            throw new IllegalArgumentException("필수 약관에 모두 동의해야 합니다. 누락된 약관 id=" + requiredTermIds);
        }
    }
}
