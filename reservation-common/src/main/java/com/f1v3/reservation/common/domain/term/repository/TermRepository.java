package com.f1v3.reservation.common.domain.term.repository;

import com.f1v3.reservation.common.domain.term.Term;
import com.f1v3.reservation.common.domain.term.enums.TermCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 약관 JPA Repository 인터페이스.
 *
 * @author Seungjo, Jeong
 */
public interface TermRepository extends JpaRepository<Term, Term.TermPk>, TermRepositoryCustom {

    @Query("SELECT MAX(t.pk.version) FROM Term t WHERE t.pk.code = :termCode")
    Optional<Integer> findMaxVersionByCode(@Param("termCode") TermCode termCode);

    Optional<Term> findByTermPkCodeAndTermPkVersion(TermCode code, Integer version);
}
