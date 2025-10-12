package com.f1v3.reservation.common.domain.term.repository;

import com.f1v3.reservation.common.domain.term.Term;
import com.f1v3.reservation.common.domain.term.enums.TermCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 약관 JPA Repository 인터페이스.
 *
 * @author Seungjo, Jeong
 */
public interface TermRepository extends JpaRepository<Term, Term.Pk>, TermRepositoryCustom {

    @Query("SELECT MAX(t.pk.version) FROM Term t WHERE t.pk.code = :termCode")
    Optional<Integer> findMaxVersionByCode(@Param("termCode") TermCode termCode);

    @Modifying
    @Query("UPDATE Term t SET t.deactivatedAt = :deactivatedAt WHERE t.pk.code = :termCode AND t.deactivatedAt IS NULL")
    void deactivateBeforeTerm(@Param("termCode") TermCode termCode, @Param("deactivatedAt") LocalDateTime deactivatedAt);

    Optional<Term> findByPkCodeAndPkVersion(TermCode code, Integer version);

}
