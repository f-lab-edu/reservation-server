package com.f1v3.reservation.api.term;

import com.f1v3.reservation.api.term.dto.TermResponse;
import com.f1v3.reservation.common.domain.term.dto.CurrentTermDto;
import com.f1v3.reservation.common.domain.term.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자용 약관 서비스
 *
 * @author Seungjo, Jeong
 */
@Service
@RequiredArgsConstructor
public class TermService {

    private final TermRepository termRepository;

    /**
     * 활성화된 약관들의 최신 버전 조회 (회원가입용)
     */
    @Transactional(readOnly = true)
    public List<TermResponse> getActiveTerms() {
        List<CurrentTermDto> activeTermsWithVersion = termRepository.getActiveTermsWithVersion();

        return activeTermsWithVersion.stream()
                .map(TermResponse::from)
                .toList();
    }
}

