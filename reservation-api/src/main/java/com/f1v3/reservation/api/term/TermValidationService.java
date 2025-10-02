package com.f1v3.reservation.api.term;

import com.f1v3.reservation.api.term.dto.TermResponse;
import com.f1v3.reservation.api.user.dto.SignupUserRequest;
import com.f1v3.reservation.common.domain.term.enums.TermCode;
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
        Set<TermCode> requiredTermIds = termService.getActiveTerms().stream()
                .filter(TermResponse::isRequired)
                .map(TermResponse::termCode)
                .collect(Collectors.toSet());

        Set<TermCode> agreedTermIds = termRequests.stream()
                .map(request -> TermCode.getCode(request.termCode())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid termCode: " + request.termCode())))
                .collect(Collectors.toSet());

        requiredTermIds.removeAll(agreedTermIds);

        if (!requiredTermIds.isEmpty()) {
            throw new IllegalArgumentException("필수 약관에 모두 동의해야 합니다. 누락된 약관 id=" + requiredTermIds);
        }
    }
}
