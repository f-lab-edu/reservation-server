package com.f1v3.reservation.admin.term;

import com.f1v3.reservation.admin.term.dto.CreateTermVersionRequest;
import com.f1v3.reservation.admin.term.dto.CreateTermVersionResponse;
import com.f1v3.reservation.common.domain.term.Term;
import com.f1v3.reservation.common.domain.term.TermVersion;
import com.f1v3.reservation.common.domain.term.repository.TermRepository;
import com.f1v3.reservation.common.domain.term.repository.TermVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 약관 버전 서비스
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TermVersionService {

    private final TermRepository termRepository;
    private final TermVersionRepository termVersionRepository;

    @Transactional
    public CreateTermVersionResponse create(long termId, CreateTermVersionRequest request) {

        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관입니다. id: " + termId));

        int nextVersion = getNextTermVersion(termId);

        TermVersion termVersion = TermVersion.builder()
                .term(term)
                .version(nextVersion)
                .content(request.content())
                .isCurrent(request.isCurrent())
                .effectiveDateTime(request.effectiveDateTime())
                .expiryDateTime(request.expiryDateTime())
                .build();

        TermVersion savedTermVersion = saveWithConstraintCheck(termVersion, term);

        // todo: 만약 isCurrent = ture 인 경우 기존 버전의 isCurrent = false + expiryDateTime 업데이트 로직 추가 필요
        return new CreateTermVersionResponse(savedTermVersion.getId());
    }

    /**
     * 다음 약관 버전 번호 조회
     */
    private int getNextTermVersion(Long termId) {
        return termVersionRepository.findMaxVersionByTermId(termId)
                .map(version -> version + 1)
                .orElse(1);
    }

    /**
     * 약관 버전 테이블의 유니크 제약 조건을 체크하며 저장
     */
    private TermVersion saveWithConstraintCheck(TermVersion termVersion, Term term) {
        try {
            return termVersionRepository.save(termVersion);
        } catch (DataIntegrityViolationException e) {
            log.error("약관 버전 저장 중 제약 조건 위반 발생: 약관명 = [{}({})], 버전 = {}", term.getTitle(), term.getCode(), termVersion.getVersion(), e);
            throw new IllegalArgumentException("이미 존재하는 약관 버전입니다.");
        }
    }
}

