package com.f1v3.reservation.common.domain.term.repository;

import com.f1v3.reservation.common.domain.term.QTerm;
import com.f1v3.reservation.common.domain.term.dto.ActiveTermDto;
import com.f1v3.reservation.common.domain.term.dto.AdminTermDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 약관 Querydsl Repository 구현체
 *
 * @author Seungjo, Jeong
 */
@RequiredArgsConstructor
public class TermRepositoryImpl implements TermRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QTerm term = QTerm.term;

    @Override
    public List<ActiveTermDto> getActiveTerms() {
        return queryFactory
                .select(
                        Projections.constructor(
                                ActiveTermDto.class,
                                term.termPk.code,
                                term.termPk.version,
                                term.title,
                                term.content,
                                term.isRequired
                        ))
                .from(term)
                .where(isActive(LocalDateTime.now()))
                .orderBy(term.termPk.code.asc(), term.termPk.version.desc(), term.createdAt.desc())
                .fetch();
    }

    @Override
    public List<AdminTermDto> getPagedTerms(Pageable pageable) {

        // todo: no offset 방식으로 변경하여 성능 개선 고려하기
        return queryFactory
                .select(
                        Projections.constructor(
                                AdminTermDto.class,
                                term.termPk.code,
                                term.termPk.version,
                                term.title,
                                term.content,
                                term.isRequired,
                                term.activatedAt,
                                term.deactivatedAt,
                                term.createdAt,
                                term.updatedAt))
                .from(term)
                .orderBy(term.termPk.code.asc(), term.termPk.version.desc(), term.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * 활성 시간 <= 시간 < 비활성 시간 or 비활성 시간이 null(제한 X)
     */
    private Predicate isActive(LocalDateTime time) {
        return term.activatedAt.loe(time)
                .and(term.deactivatedAt.gt(time).or(term.deactivatedAt.isNull()));
    }

}
