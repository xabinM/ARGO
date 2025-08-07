package com.argo.backend.organization.dto.classlist;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PaginationDto {
    
    private long totalCount;
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrev;
    
    public PaginationDto(long totalCount, int currentPage, int totalPages, int pageSize, boolean hasNext, boolean hasPrev) {
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
    }
    
    public static PaginationDto from(Page<?> page) {
        return new PaginationDto(
                page.getTotalElements(),
                page.getNumber() + 1, // 0-based를 1-based로 변환
                page.getTotalPages(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}