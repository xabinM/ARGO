package com.argo.backend.organization.dto.teamdelete;

import lombok.Getter;

import java.util.List;

@Getter
public class TeamDeleteResponse {
    
    private DeletedTeamDto deletedTeam;
    private List<UnassignedStudentDto> unassignedStudents;
    private ClassTeamStatusDto classTeamStatus;
    
    public TeamDeleteResponse(DeletedTeamDto deletedTeam, List<UnassignedStudentDto> unassignedStudents, 
                             ClassTeamStatusDto classTeamStatus) {
        this.deletedTeam = deletedTeam;
        this.unassignedStudents = unassignedStudents;
        this.classTeamStatus = classTeamStatus;
    }
    
    public static TeamDeleteResponse of(DeletedTeamDto deletedTeam, List<UnassignedStudentDto> unassignedStudents,
                                       ClassTeamStatusDto classTeamStatus) {
        return new TeamDeleteResponse(deletedTeam, unassignedStudents, classTeamStatus);
    }
}