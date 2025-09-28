package com.f1v3.reservation.api.term;

import com.f1v3.reservation.api.term.dto.TermResponse;
import com.f1v3.reservation.api.user.dto.SignupUserRequest;
import com.f1v3.reservation.common.domain.term.enums.TermType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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
        Set<Long> requiredTermIds = getRequiredTermIds();
        Set<Long> agreedTermIds = getAgreedTermIds(termRequests);

        Set<Long> missingRequiredTerms = getMissingRequiredTerms(requiredTermIds, agreedTermIds);

        if (!missingRequiredTerms.isEmpty()) {
            throw new IllegalArgumentException("필수 약관에 모두 동의해야 합니다. 누락된 약관 id=" + missingRequiredTerms);
        }
    }

    private Set<Long> getMissingRequiredTerms(Set<Long> requiredTermIds, Set<Long> agreedTermIds) {
        Set<Long> missingAgreements = new HashSet<>(requiredTermIds);
        missingAgreements.removeAll(agreedTermIds);
        return missingAgreements;
    }

    private Set<Long> getRequiredTermIds() {
        return termService.getActiveTerms().stream()
                .filter(term -> TermType.REQUIRED.name().equals(term.getType()))
                .map(TermResponse::getTermId)
                .collect(Collectors.toSet());
    }

    private Set<Long> getAgreedTermIds(Set<SignupUserRequest.SignupTermRequest> termRequests) {
        return termRequests.stream()
                .map(SignupUserRequest.SignupTermRequest::termId)
                .collect(Collectors.toSet());
    }
}
