package com.f1v3.reservation.common.domain.accommodation.repository;

import com.f1v3.reservation.common.domain.accommodation.Accommodation;
import com.f1v3.reservation.common.domain.accommodation.dto.SearchAccommodationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 숙소 엔티티의 JPA Repository 인터페이스
 *
 * @author Seungjo, Jeong
 */
public interface AccommodationRepository extends JpaRepository<Accommodation, Long>,
        AccommodationRepositoryCustom {

    List<Accommodation> findBySupplierId(Long supplierId);

    @Query(value = """
            SELECT
                a.id AS id,
                a.name AS name,
                a.address AS address,
                a.thumbnail AS thumbnail,
                MIN(rt.base_price) AS minPrice
            FROM accommodations a
            JOIN room_types rt ON rt.accommodation_id = a.id
            LEFT JOIN reservations r
                ON r.room_type_id = rt.id
                AND r.check_in < :checkOut
                AND r.check_out > :checkIn
            WHERE a.is_visible = TRUE
              AND MATCH(a.name, a.address) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
              AND rt.standard_capacity <= :capacity
              AND rt.max_capacity >= :capacity
            GROUP BY a.id, a.name, a.address, a.thumbnail
            HAVING COUNT(r.id) < SUM(rt.total_room_count)
            ORDER BY MATCH(a.name, a.address) AGAINST (:keyword IN NATURAL LANGUAGE MODE) DESC, a.id ASC
            """,
            countQuery = """
                    SELECT COUNT(*) FROM (
                        SELECT a.id
                        FROM accommodations a
                        JOIN room_types rt ON rt.accommodation_id = a.id
                        LEFT JOIN reservations r
                            ON r.room_type_id = rt.id
                            AND r.check_in < :checkOut
                            AND r.check_out > :checkIn
                        WHERE a.is_visible = TRUE
                          AND MATCH(a.name, a.address) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
                          AND rt.standard_capacity <= :capacity
                          AND rt.max_capacity >= :capacity
                        GROUP BY a.id, a.name, a.address, a.thumbnail
                        HAVING COUNT(r.id) < SUM(rt.total_room_count)
                    ) counted
                    """,
            nativeQuery = true)
    Page<SearchAccommodationDto> searchFullText(
            @Param("keyword") String keyword,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("capacity") int capacity,
            Pageable pageable
    );
}
