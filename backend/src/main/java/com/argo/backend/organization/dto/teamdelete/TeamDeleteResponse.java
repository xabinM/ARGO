package com.argo.backend.organization.dto.teamdelete;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TeamDeleteResponse {
    private final DeletedTeamInfo deletedTeam;
    private final List<UnassignedStudentInfo> unassignedStudents;
    private final ClassTeamStatus classTeamStatus;
}