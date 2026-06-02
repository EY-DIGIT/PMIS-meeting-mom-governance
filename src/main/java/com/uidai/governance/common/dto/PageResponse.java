package com.uidai.governance.common.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Serializable, stable pagination envelope (avoids exposing Spring's {@code Page}
 * internals over the API).
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
