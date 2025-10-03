package com.f1v3.reservation.api.user;

import com.f1v3.reservation.api.term.TermValidationService;
import com.f1v3.reservation.api.user.dto.SignupUserRequest;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.term.Term;
import com.f1v3.reservation.common.domain.term.enums.TermCode;
import com.f1v3.reservation.common.domain.term.repository.TermRepository;
import com.f1v3.reservation.common.domain.user.User;
import com.f1v3.reservation.common.domain.user.UserTermAgreement;
import com.f1v3.reservation.common.domain.user.repository.UserTermAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.f1v3.reservation.common.api.error.ErrorCode.TERM_NOT_FOUND;

/**
 * 사용자 약관 동의 서비스 클래스
 *
 * @author Seungjo, Jeong
 */
@Service
@RequiredArgsConstructor
public class UserTermAgreementService {

    private final TermValidationService termValidationService;
    private final UserTermAgreementRepository userTermAgreementRepository;
    private final TermRepository termRepository;

    @Transactional
    public void createAgreements(User user, Set<SignupUserRequest.SignupTermRequest> termRequests) {
        termValidationService.validateRequiredTermsAgreed(termRequests);

        List<UserTermAgreement> agreements = termRequests.stream()
                .map(term -> createAgreement(user, term))
                .toList();

        userTermAgreementRepository.saveAll(agreements);
    }

    private UserTermAgreement createAgreement(User user, SignupUserRequest.SignupTermRequest request) {
        TermCode termCode = TermCode.getCode(request.termCode())
                .orElseThrow(() -> new ReservationException(TERM_NOT_FOUND));

        Term term = termRepository.findByCodeAndVersion(termCode, request.version())
                .orElseThrow(() -> new ReservationException(TERM_NOT_FOUND));

        return UserTermAgreement.builder()
                .user(user)
                .term(term)
                .build();
    }
}
