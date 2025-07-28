package com.argo.backend.organization.dto.classroomlist;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClassListWithPaginationDto {
    private List<ClassListResponseDto> classes;
    private PaginationResponseDto pagination;
}