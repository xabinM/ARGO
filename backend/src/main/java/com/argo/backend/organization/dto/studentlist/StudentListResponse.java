package com.argo.backend.organization.dto.studentlist;

import com.argo.backend.organization.dto.classroomlist.PaginationResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StudentListResponse {
    private final StudentListClassInfo classInfo;
    private final List<StudentInfo> students;
    private final TeamSummary teamSummary;
    private final PaginationResponseDto pagination;

    public static StudentListResponse of(StudentListClassInfo classInfo, 
                                       List<StudentInfo> students,
                                       TeamSummary teamSummary,
                                       PaginationResponseDto pagination) {
        return StudentListResponse.builder()
                .classInfo(classInfo)
                .students(students)
                .teamSummary(teamSummary)
                .pagination(pagination)
                .build();
    }
}