package com.argo.backend.organization.dto.classdetail;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ClassDetailResponse {

    private final ClassInfo classInfo;
    
    private final List<StudentDto> students;
    
    private final List<TeamDto> teams;
    
    private final TeamDto myTeam; // 학생용: 본인 팀 정보
    
    private final StatisticsDto statistics;

    // 선생님용 응답 생성
    public static ClassDetailResponse forTeacher(ClassInfo classInfo, 
                                               List<StudentDto> students,
                                               List<TeamDto> teams,
                                               StatisticsDto statistics) {
        return ClassDetailResponse.builder()
                .classInfo(classInfo)
                .students(students)
                .teams(teams)
                .statistics(statistics)
                .build();
    }

    // 학생용 응답 생성 (제한된 정보)
    public static ClassDetailResponse forStudent(ClassInfo classInfo, TeamDto myTeam) {
        return ClassDetailResponse.builder()
                .classInfo(classInfo)
                .myTeam(myTeam)
                .build();
    }
}
