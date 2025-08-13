package com.argo.backend.organization.dto.classdetail;

import lombok.Getter;

@Getter
public class ClassStatisticsDto {
    
    private int totalStudents;
    private int totalTeams;
    
    public ClassStatisticsDto(int totalStudents, int totalTeams) {
        this.totalStudents = totalStudents;
        this.totalTeams = totalTeams;
    }
    
    public static ClassStatisticsDto of(int totalStudents, int totalTeams) {
        return new ClassStatisticsDto(totalStudents, totalTeams);
    }
}