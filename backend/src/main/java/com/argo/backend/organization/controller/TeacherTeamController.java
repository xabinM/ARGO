package com.argo.backend.organization.controller;

import com.argo.backend.organization.dto.CommonApiResponse;
import com.argo.backend.organization.dto.teamassign.TeamAssignResponse;
import com.argo.backend.organization.dto.teamcreate.TeamCreateRequest;
import com.argo.backend.organization.dto.teamcreate.TeamCreateResponse;
import com.argo.backend.organization.dto.teamassign.TeamAssignRequest;
import com.argo.backend.organization.dto.teamautoassign.TeamAutoAssignResponse;
import com.argo.backend.organization.dto.teamdelete.TeamDeleteResponse;
import com.argo.backend.organization.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.argo.backend.organization.message.ResponseMessage;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teacher/classes")
@Validated
@PreAuthorize("hasRole('TEACHER')")
public class TeacherTeamController {

    private final TeamService teamService;

    @PostMapping("/{classId}/teams")
    public ResponseEntity<CommonApiResponse<TeamCreateResponse>> createTeam(
            @PathVariable Long classId,
            @Valid @RequestBody TeamCreateRequest request,
            @AuthenticationPrincipal Long teacherId
    ) {
        TeamCreateResponse response = teamService.createTeam(classId, request, teacherId);
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.TEAM_CREATE_SUCCESS, response));
    }

    @PostMapping("/{classId}/teams/{teamId}/assign")
    public ResponseEntity<CommonApiResponse<TeamAssignResponse>> assignStudentsToTeam(
            @PathVariable Long classId,
            @PathVariable Long teamId,
            @Valid @RequestBody TeamAssignRequest request,
            @AuthenticationPrincipal Long teacherId
    ) {
        com.argo.backend.organization.dto.teamassign.TeamAssignResponse response = teamService.assignStudentsToTeam(classId, teamId, request, teacherId);
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.TEAM_ASSIGN_SUCCESS, response));
    }

    @PostMapping("/{classId}/teams/assign")
    public ResponseEntity<CommonApiResponse<TeamAutoAssignResponse>> autoAssignStudentsToTeams(
            @PathVariable Long classId,
            @AuthenticationPrincipal Long teacherId
    ) {
        TeamAutoAssignResponse response = teamService.autoAssignStudentsToTeams(classId, teacherId);
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.TEAM_AUTO_ASSIGN_SUCCESS, response));
    }

    @DeleteMapping("/{classId}/teams/{teamId}")
    public ResponseEntity<CommonApiResponse<TeamDeleteResponse>> deleteTeam(
            @PathVariable Long classId,
            @PathVariable Long teamId,
            @AuthenticationPrincipal Long teacherId
    ) {
        TeamDeleteResponse response = teamService.deleteTeam(classId, teamId, teacherId);
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.TEAM_DELETE_SUCCESS, response));
    }
}
