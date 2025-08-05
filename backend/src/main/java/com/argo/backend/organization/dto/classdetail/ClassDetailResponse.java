package com.argo.backend.organization.dto.classdetail;

import lombok.Getter;

@Getter
public class ClassDetailResponse {
    
    private ClassInfoDetailDto classInfo;
    private java.util.List<StudentDto> students;
    private java.util.List<TeamDetailDto> teams;
    private TeamDetailDto myTeam;
    private StatisticsDto statistics;
    
    public ClassDetailResponse(ClassInfoDetailDto classInfo, java.util.List<StudentDto> students, 
                              java.util.List<TeamDetailDto> teams, TeamDetailDto myTeam, StatisticsDto statistics) {
        this.classInfo = classInfo;
        this.students = students;
        this.teams = teams;
        this.myTeam = myTeam;
        this.statistics = statistics;
    }
    
    // 선생님용
    public static ClassDetailResponse forTeacher(ClassInfoDetailDto classInfo, java.util.List<StudentDto> students, 
                                                java.util.List<TeamDetailDto> teams, StatisticsDto statistics) {
        return new ClassDetailResponse(classInfo, students, teams, null, statistics);
    }
    
    // 학생용
    public static ClassDetailResponse forStudent(ClassInfoDetailDto classInfo, TeamDetailDto myTeam) {
        return new ClassDetailResponse(classInfo, null, null, myTeam, null);
    }
}