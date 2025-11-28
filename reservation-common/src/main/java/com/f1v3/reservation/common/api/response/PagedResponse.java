package com.f1v3.reservation.common.api.response;

import java.util.List;
import java.util.function.Function;

/**
 * 페이징 결과 응답 객체
 *
 * @author Seungjo, Jeong
 */
public record PagedResponse<T>(
        List<T> content,
        PageInfo page
) {

    public static <T> PagedResponse<T> of(List<T> content, PageInfo pageInfo) {
        return new PagedResponse<>(content, pageInfo);
    }

    public <R> PagedResponse<R> map(Function<T, R> mapper) {
        List<R> mapped = content.stream()
                .map(mapper)
                .toList();

        return PagedResponse.of(mapped, page);
    }
}
