package com.f1v3.reservation.common.domain.accommodation.repository;

import com.f1v3.reservation.common.domain.accommodation.dto.SearchAccommodationDto;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * 숙소 엔티티의 커스텀 Repository 인터페이스
 *
 * @author Seungjo, Jeong
 */
@NoRepositoryBean
public interface AccommodationRepositoryCustom {

    List<SearchAccommodationDto> findContainingNameOrRegion(String keyword);
}
