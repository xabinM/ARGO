package com.argo.backend.organization.dto.studentlist;

import lombok.Getter;

import java.util.List;

@Getter
public class TeamSummaryDto {
    
    private int totalTeams;
    private int assignedStudents;
    private int unassignedStudents;
    private List<TeamStatusDto> teams;
    
    public TeamSummaryDto(int totalTeams, int assignedStudents, int unassignedStudents, 
                         List<TeamStatusDto> teams) {
        this.totalTeams = totalTeams;
        this.assignedStudents = assignedStudents;
        this.unassignedStudents = unassignedStudents;
        this.teams = teams;
    }
    
    public static TeamSummaryDto of(int totalTeams, int assignedStudents, int unassignedStudents,
                                   List<TeamStatusDto> teams) {
        return new TeamSummaryDto(totalTeams, assignedStudents, unassignedStudents, teams);
    }
}