package com.argo.backend.organization.dto.studentlist;

import lombok.Getter;

import java.util.List;

@Getter
public class StudentListResponse {
    
    private StudentClassInfoDto classInfo;
    private List<StudentDetailDto> students;
    private TeamSummaryDto teamSummary;
    private StudentListPaginationDto pagination;
    
    public StudentListResponse(StudentClassInfoDto classInfo, List<StudentDetailDto> students, 
                             TeamSummaryDto teamSummary, StudentListPaginationDto pagination) {
        this.classInfo = classInfo;
        this.students = students;
        this.teamSummary = teamSummary;
        this.pagination = pagination;
    }
    
    public static StudentListResponse of(StudentClassInfoDto classInfo, List<StudentDetailDto> students, 
                                       TeamSummaryDto teamSummary, StudentListPaginationDto pagination) {
        return new StudentListResponse(classInfo, students, teamSummary, pagination);
    }
}