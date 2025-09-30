package com.f1v3.reservation.common.domain.term.repository;

import com.f1v3.reservation.common.domain.term.dto.ActiveTermDto;
import com.f1v3.reservation.common.domain.term.dto.AdminTermDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * 약관 Querydsl Repository 커스텀 인터페이스
 *
 * @author Seungjo, Jeong
 */
@NoRepositoryBean
public interface TermRepositoryCustom {
    List<ActiveTermDto> getActiveTerms();
    List<AdminTermDto> getPagedTerms(Pageable pageable);
}
