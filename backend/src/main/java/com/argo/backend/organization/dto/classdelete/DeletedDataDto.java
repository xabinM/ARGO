package com.argo.backend.organization.dto.classdelete;

import lombok.Getter;

@Getter
public class DeletedDataDto {
    
    private DeletedStudentsDto students;
    private DeletedTeamsDto teams;
    private DeletedApplicationsDto applications;
    
    public DeletedDataDto(DeletedStudentsDto students, DeletedTeamsDto teams, DeletedApplicationsDto applications) {
        this.students = students;
        this.teams = teams;
        this.applications = applications;
    }
    
    public static DeletedDataDto of(DeletedStudentsDto students, DeletedTeamsDto teams, DeletedApplicationsDto applications) {
        return new DeletedDataDto(students, teams, applications);
    }
}