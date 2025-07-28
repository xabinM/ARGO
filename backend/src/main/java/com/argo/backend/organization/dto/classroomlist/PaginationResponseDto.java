package com.argo.backend.organization.dto.classroomlist;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationResponseDto {
    private Long totalCount;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrev;
}