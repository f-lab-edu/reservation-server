package com.f1v3.reservation.common.api.response;

import org.springframework.data.domain.Pageable;

/**
 * 페이지 정보 객체
 *
 * @author Seungjo, Jeong
 */
public record PageInfo(
        int page,
        int size,
        long totalElements,
        long totalPages,
        boolean hasPrevious,
        boolean hasNext
) {

    public static PageInfo of(int page, int size, long totalElements) {
        long totalPages = (size <= 0 || totalElements == 0) ? 0 : (long) Math.ceil((double) totalElements / size);
        boolean hasPrevious = page > 0;
        boolean hasNext = size > 0 && page + 1 < totalPages;
        return new PageInfo(page, size, totalElements, totalPages, hasPrevious, hasNext);
    }

    public static PageInfo of(Pageable pageable, long totalElements) {
        return of(pageable.getPageNumber(), pageable.getPageSize(), totalElements);
    }
}
