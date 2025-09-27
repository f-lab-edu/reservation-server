package com.f1v3.reservation.admin.term;

import com.f1v3.reservation.admin.term.dto.CreateTermRequest;
import com.f1v3.reservation.admin.term.dto.CreateTermResponse;
import com.f1v3.reservation.admin.term.dto.TermResponse;
import com.f1v3.reservation.common.domain.term.Term;
import com.f1v3.reservation.common.domain.term.dto.AdminTermDto;
import com.f1v3.reservation.common.domain.term.enums.TermCode;
import com.f1v3.reservation.common.domain.term.enums.TermStatus;
import com.f1v3.reservation.common.domain.term.enums.TermType;
import com.f1v3.reservation.common.domain.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 관리자용 약관 서비스
 *
 * @author Seungjo, Jeong
 */
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

    public CreateTermResponse create(CreateTermRequest request) {

        Term term = Term.builder()
                .code(TermCode.valueOf(request.code()))
                .title(request.title())
                .type(TermType.valueOf(request.type()))
                .displayOrder(request.displayOrder())
                .status(TermStatus.valueOf(request.status()))
                .build();

        Term savedTerm = termRepository.save(term);

        return new CreateTermResponse(savedTerm.getId());
    }
}
