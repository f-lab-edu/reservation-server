package com.f1v3.reservation.admin.term;

import com.f1v3.reservation.admin.term.dto.CreateTermRequest;
import com.f1v3.reservation.admin.term.dto.CreateTermResponse;
import com.f1v3.reservation.admin.term.dto.TermResponse;
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

    public List<TermResponse> getPagedTerms(Pageable pageable) {
        List<AdminTermDto> pagedTerms = termRepository.getPagedTerms(pageable);

        return pagedTerms.stream()
                .map(TermResponse::from)
                .toList();
    }

    @Transactional
    public CreateTermResponse create(CreateTermRequest request) {
        TermCode termCode = TermCode.getCode(request.code())
                .orElseThrow(() -> new IllegalArgumentException("Invalid term code: " + request.code()));

        int nextVersion = termRepository.findMaxVersionByCode(termCode)
                .map(version -> version + 1)
                .orElse(1);

        if (nextVersion > 1) {
            termRepository.deactivateBeforeTerm(termCode, request.activatedAt());
        }

        Term newTerm = Term.builder()
                .code(termCode)
                .title(request.title())
                .content(request.content())
                .version(nextVersion)
                .displayOrder(request.displayOrder())
                .isRequired(request.isRequired())
                .activatedAt(request.activatedAt())
                .deactivatedAt(request.deactivatedAt())
                .build();

        Term savedTerm = saveWithConstraintCheck(newTerm);
        return new CreateTermResponse(savedTerm.getId(), savedTerm.getCode().name(), savedTerm.getVersion());
    }

    private Term saveWithConstraintCheck(Term term) {
        try {
            return termRepository.save(term);
        } catch (DataIntegrityViolationException e) {
            log.error("Term constraint violation: {}", e.getMessage());
            throw new IllegalStateException("Term constraint violation", e);
        }
    }
}
