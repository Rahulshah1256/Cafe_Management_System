package com.inn.cafe.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Small helper to build a Spring Data {@link Pageable} from raw page/size/sortBy/direction
 * request params, used by the new paginated list endpoints (getAllProductPaged, etc.).
 */
public class PageUtils {

    private PageUtils() {
    }

    public static Pageable buildPageable(int page, int size, String sortBy, String direction) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        return PageRequest.of(safePage, safeSize, Sort.by(sortDirection, safeSortBy));
    }
}
