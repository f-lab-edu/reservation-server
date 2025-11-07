package com.f1v3.reservation.common.domain.accommodation.repository;

import com.f1v3.reservation.common.domain.accommodation.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 숙소 엔티티의 JPA Repository 인터페이스
 *
 * @author Seungjo, Jeong
 */
public interface AccommodationRepository extends JpaRepository<Accommodation, Long>, AccommodationRepositoryCustom {

    List<Accommodation> findBySupplierId(Long supplierId);

    @Query(value = "SELECT *" +
            " FROM accommodations" +
            " WHERE MATCH(name, address, description) AGAINST(?1)" +
            " LIMIT 30",
            nativeQuery = true)
    List<Accommodation> fullTextSearchByKeyword(String keyword);
}
