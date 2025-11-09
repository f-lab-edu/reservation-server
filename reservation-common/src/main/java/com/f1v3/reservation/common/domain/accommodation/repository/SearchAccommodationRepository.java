package com.f1v3.reservation.common.domain.accommodation.repository;

import com.f1v3.reservation.common.domain.accommodation.dto.SearchAccommodationDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 숙소 검색을 위한 커스텀 Repository 구현체
 *
 * @author Seungjo, Jeong
 */
@Repository
@RequiredArgsConstructor
public class SearchAccommodationRepository {

    private final EntityManager entityManager;

    public List<SearchAccommodationDto> search(
            String keyword, LocalDate checkIn, LocalDate checkOut,
            int capacity, int page, int size
    ) {

        String sql = """
                WITH available_rt AS (
                    SELECT rt.accommodation_id, rt.base_price, COUNT(r.id) as booked_count, rt.total_room_count
                    FROM room_types rt
                    LEFT JOIN reservations r ON (
                        r.room_type_id = rt.id
                        AND r.check_in < :checkOut
                        AND r.check_out > :checkIn
                    )
                    WHERE rt.standard_capacity <= :capacity
                      AND rt.max_capacity >= :capacity
                    GROUP BY rt.id, rt.accommodation_id, rt.base_price, rt.total_room_count
                    HAVING booked_count < rt.total_room_count
                )
                
                SELECT a.id, a.name, a.address, a.thumbnail, MIN(available_rt.base_price) AS min_price
                FROM accommodations a
                JOIN available_rt ON a.id = available_rt.accommodation_id
                WHERE a.is_visible = true
                  AND MATCH(a.name, a.address) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
                GROUP BY a.id, a.name, a.address, a.thumbnail
                LIMIT :limit OFFSET :offset
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(sql)
                .setParameter("keyword", keyword)
                .setParameter("capacity", capacity)
                .setParameter("checkIn", checkIn)
                .setParameter("checkOut", checkOut)
                .setParameter("limit", size)
                .setParameter("offset", page * size)
                .getResultList();

        return results.stream()
                .map(row -> new SearchAccommodationDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        (String) row[3],
                        (BigDecimal) row[4]
                ))
                .toList();
    }
}
