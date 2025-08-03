package com.argo.backend.organization.dto.teamdelete;

import lombok.Getter;

@Getter
public class ClassTeamStatusDto {
    
    private int totalTeams;
    private int totalStudents;
    private int assignedStudents;
    private int unassignedStudents;
    
    public ClassTeamStatusDto(int totalTeams, int totalStudents, int assignedStudents, int unassignedStudents) {
        this.totalTeams = totalTeams;
        this.totalStudents = totalStudents;
        this.assignedStudents = assignedStudents;
        this.unassignedStudents = unassignedStudents;
    }
    
    public static ClassTeamStatusDto of(int totalTeams, int totalStudents, int assignedStudents, int unassignedStudents) {
        return new ClassTeamStatusDto(totalTeams, totalStudents, assignedStudents, unassignedStudents);
    }
}