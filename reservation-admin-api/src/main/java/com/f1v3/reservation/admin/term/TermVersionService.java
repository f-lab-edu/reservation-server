package com.f1v3.reservation.admin.term;

import com.f1v3.reservation.admin.term.dto.CreateTermVersionRequest;
import com.f1v3.reservation.admin.term.dto.CreateTermVersionResponse;
import com.f1v3.reservation.common.domain.term.Term;
import com.f1v3.reservation.common.domain.term.TermVersion;
import com.f1v3.reservation.common.domain.term.repository.TermRepository;
import com.f1v3.reservation.common.domain.term.repository.TermVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {class name}.
 *
 * @author Seungjo, Jeong
 */
@Service
@RequiredArgsConstructor
public class TermVersionService {

    private final TermRepository termRepository;
    private final TermVersionRepository termVersionRepository;

    public CreateTermVersionResponse create(Long termId, CreateTermVersionRequest request) {

        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관입니다. id: " + termId));

        // fixme: 버전을 직접 설정하지 않고, 생성 시점에 자동 증가하도록 변경하는 방향
        TermVersion termVersion = TermVersion.builder()
                .term(term)
                .version(request.version()) // <- 수정해야 하는 부분
                .content(request.content())
                .isCurrent(request.isCurrent())
                .effectiveDateTime(request.effectiveDateTime())
                .expiryDateTime(request.expiryDateTime())
                .build();

        TermVersion savedTermVersion = termVersionRepository.save(termVersion);
        return new CreateTermVersionResponse(savedTermVersion.getId());
    }
}

