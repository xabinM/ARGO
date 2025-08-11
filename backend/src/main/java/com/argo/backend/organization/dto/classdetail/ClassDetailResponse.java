package com.argo.backend.organization.dto.classdetail;

import lombok.Getter;

import java.util.List;

@Getter
public class ClassDetailResponse {
    
    private ClassInfoDetailDto classInfo;
    private List<StudentDto> students;
    private List<TeamDetailDto> teams;
    private ClassStatisticsDto statistics;
    
    public ClassDetailResponse(ClassInfoDetailDto classInfo, List<StudentDto> students,
                              List<TeamDetailDto> teams, ClassStatisticsDto statistics) {
        this.classInfo = classInfo;
        this.students = students;
        this.teams = teams;
        this.statistics = statistics;
    }
    
    // 공통
    public static ClassDetailResponse from(ClassInfoDetailDto classInfo, List<StudentDto> students,
                                           List<TeamDetailDto> teams, ClassStatisticsDto statistics) {
        return new ClassDetailResponse(classInfo, students, teams, statistics);
    }
}