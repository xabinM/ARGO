package com.argo.backend.organization.dto.classdetail;

import lombok.Getter;

@Getter
public class StatisticsDto {
    
    private int totalStudents;
    private int totalTeams;
    
    public StatisticsDto(int totalStudents, int totalTeams) {
        this.totalStudents = totalStudents;
        this.totalTeams = totalTeams;
    }
    
    public static StatisticsDto of(int totalStudents, int totalTeams) {
        return new StatisticsDto(totalStudents, totalTeams);
    }
}