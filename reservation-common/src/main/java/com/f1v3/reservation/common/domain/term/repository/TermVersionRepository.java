package com.f1v3.reservation.common.domain.term.repository;

import com.f1v3.reservation.common.domain.term.TermVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 약관 버전 JPA Repository 인터페이스.
 *
 * @author Seungjo, Jeong
 */
public interface TermVersionRepository extends JpaRepository<TermVersion, Long> {

    @Query("SELECT MAX(tv.version) FROM TermVersion  tv WHERE tv.term.id = :termId")
    Optional<Integer> findMaxVersionByTermId(@Param("termId") Long termId);
}
