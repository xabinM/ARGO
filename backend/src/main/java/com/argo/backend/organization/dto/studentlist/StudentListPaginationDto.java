package com.argo.backend.organization.dto.studentlist;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class StudentListPaginationDto {
    
    private long totalCount;
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrev;
    
    public StudentListPaginationDto(long totalCount, int currentPage, int totalPages, 
                                   int pageSize, boolean hasNext, boolean hasPrev) {
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
    }
    
    public static StudentListPaginationDto from(Page<?> page) {
        return new StudentListPaginationDto(
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getTotalPages(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}