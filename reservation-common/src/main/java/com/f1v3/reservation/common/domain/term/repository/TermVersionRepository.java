package com.f1v3.reservation.common.domain.term.repository;

import com.f1v3.reservation.common.domain.term.TermVersion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 약관 버전 JPA Repository 인터페이스.
 *
 * @author Seungjo, Jeong
 */
public interface TermVersionRepository extends JpaRepository<TermVersion, Long> {
}
