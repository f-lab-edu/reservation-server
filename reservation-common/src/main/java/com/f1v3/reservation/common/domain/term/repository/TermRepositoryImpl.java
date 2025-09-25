package com.f1v3.reservation.common.domain.term.repository;

import com.f1v3.reservation.common.domain.term.QTerm;
import com.f1v3.reservation.common.domain.term.QTermVersion;
import com.f1v3.reservation.common.domain.term.dto.CurrentTermDto;
import com.f1v3.reservation.common.domain.term.enums.TermStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 약관 Querydsl Repository 구현체
 *
 * @author Seungjo, Jeong
 */
@RequiredArgsConstructor
public class TermRepositoryImpl implements TermRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CurrentTermDto> getActiveTermsWithVersion() {
        QTerm term = QTerm.term;
        QTermVersion termVersion = QTermVersion.termVersion;

        return queryFactory
                .select(
                        Projections.constructor(
                                CurrentTermDto.class,
                                term.id,
                                term.code,
                                term.title,
                                term.type,
                                term.displayOrder,
                                termVersion.version,
                                termVersion.content))
                .from(term)
                .join(termVersion).on(termVersion.term.eq(term)
                        .and(termVersion.isCurrent.isTrue()))
                .where(term.status.eq(TermStatus.ACTIVE))
                .orderBy(term.displayOrder.asc())
                .fetch();
    }
}
