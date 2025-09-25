package com.f1v3.reservation.common.domain.term.repository;

import com.f1v3.reservation.common.domain.term.Term;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 약관 JPA Repository 인터페이스.
 *
 * @author Seungjo, Jeong
 */
public interface TermRepository extends JpaRepository<Term, Long>, TermRepositoryCustom {
}
