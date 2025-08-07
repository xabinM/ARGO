package com.argo.backend.organization.dto.applicationlist;

import org.springframework.data.domain.Page;

public record PaginationDto(
    Long totalCount,
    Integer currentPage,
    Integer totalPages,
    Integer pageSize,
    Boolean hasNext,
    Boolean hasPrev
) {
    public static PaginationDto from(Page<?> page) {
        return new PaginationDto(
                page.getTotalElements(),
                page.getNumber() + 1, // 0-based to 1-based
                page.getTotalPages(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}