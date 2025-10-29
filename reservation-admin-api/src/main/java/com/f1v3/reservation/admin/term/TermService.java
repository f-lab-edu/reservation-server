package com.f1v3.reservation.admin.term;

import com.f1v3.reservation.admin.term.dto.CreateTermRequest;
import com.f1v3.reservation.admin.term.dto.CreateTermResponse;
import com.f1v3.reservation.admin.term.dto.TermResponse;
import com.f1v3.reservation.common.api.error.ErrorCode;
import com.f1v3.reservation.common.api.error.ReservationException;
import com.f1v3.reservation.common.domain.term.Term;
import com.f1v3.reservation.common.domain.term.dto.AdminTermDto;
import com.f1v3.reservation.common.domain.term.enums.TermCode;
import com.f1v3.reservation.common.domain.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.f1v3.reservation.common.api.error.ErrorCode.TERM_CODE_INVALID;
import static com.f1v3.reservation.common.api.error.ErrorCode.TERM_NOT_FOUND;

/**
 * 관리자용 약관 서비스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TermService {

    private final TermRepository termRepository;

    private static final int EMPTY_VERSION = 0;

    public List<TermResponse> getPagedTerms(Pageable pageable) {
        List<AdminTermDto> pagedTerms = termRepository.getPagedTerms(pageable);

        return pagedTerms.stream()
                .map(TermResponse::from)
                .toList();
    }

    @Transactional
    public CreateTermResponse create(CreateTermRequest request) {

        TermCode termCode = TermCode.getCode(request.code())
                .orElseThrow(() -> new ReservationException(TERM_CODE_INVALID, log::warn));

        int currentVersion = termRepository.findMaxVersionByCode(termCode)
                .orElse(EMPTY_VERSION);

        if (currentVersion > EMPTY_VERSION) {
            termRepository.findById(new Term.TermPk(termCode, currentVersion))
                    .orElseThrow(() -> new ReservationException(TERM_NOT_FOUND, log::warn))
                    .changeDeactivatedAt(request.activatedAt());
        }

        Term newTerm = Term.builder()
                .termPk(new Term.TermPk(termCode, currentVersion + 1))
                .title(request.title())
                .content(request.content())
                .isRequired(request.isRequired())
                .activatedAt(request.activatedAt())
                .deactivatedAt(request.deactivatedAt())
                .build();

        saveWithConstraintCheck(newTerm);

        return new CreateTermResponse(
                newTerm.getTermPk().getCode().name(),
                newTerm.getTermPk().getVersion()
        );
    }

    private void saveWithConstraintCheck(Term term) {
        try {
            termRepository.saveAndFlush(term);
        } catch (DataIntegrityViolationException e) {
            log.info("exception = {}", e.getClass());
            Map<String, Object> parameters = Map.of(
                    "termCode", term.getTermPk().getCode(),
                    "termVersion", term.getTermPk().getVersion()
            );

            throw new ReservationException(ErrorCode.TERM_VERSION_CONSTRAINT_VIOLATION, log::error, parameters, e);
        }
    }
}
