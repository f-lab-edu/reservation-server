package com.f1v3.reservation.common.domain.term.repository;

import com.f1v3.reservation.common.domain.term.QTerm;
import com.f1v3.reservation.common.domain.term.QTermVersion;
import com.f1v3.reservation.common.domain.term.dto.AdminTermDto;
import com.f1v3.reservation.common.domain.term.dto.CurrentTermDto;
import com.f1v3.reservation.common.domain.term.enums.TermStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

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
    private final QTermVersion termVersion = QTermVersion.termVersion;


    @Override
    public List<CurrentTermDto> getActiveTermsWithVersion() {
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

    @Override
    public List<AdminTermDto> getPagedTerms(Pageable pageable) {

        // todo: no offset 방식으로 변경하여 성능 개선 고려하기
        return queryFactory
                .select(
                        Projections.constructor(
                                AdminTermDto.class,
                                term.id,
                                term.code,
                                term.title,
                                term.type,
                                term.displayOrder,
                                term.status,
                                term.createdAt,
                                term.updatedAt,
                                termVersion.id,
                                termVersion.version,
                                termVersion.isCurrent,
                                termVersion.content,
                                termVersion.effectiveDateTime,
                                termVersion.expiryDateTime,
                                termVersion.createdAt,
                                termVersion.updatedAt
                        ))
                .from(term)
                .leftJoin(termVersion).on(termVersion.term.eq(term))
                .orderBy(term.id.desc(), termVersion.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

}
