package com.argo.backend.organization.controller;

import com.argo.backend.organization.dto.CommonApiResponse;
import com.argo.backend.organization.dto.teamcreate.TeamCreateRequest;
import com.argo.backend.organization.dto.teamcreate.TeamCreateResponse;
import com.argo.backend.organization.dto.teamassign.TeamAssignRequest;
import com.argo.backend.organization.dto.teamassign.TeamAssignResponse;
import com.argo.backend.organization.dto.teamautoassign.TeamAutoAssignResponse;
import com.argo.backend.organization.dto.teamdelete.TeamDeleteResponse;
import com.argo.backend.organization.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teacher/classes")
@Validated
public class TeacherTeamController {

    private final TeamService teamService;

    @PostMapping("/{classId}/teams")
    public ResponseEntity<CommonApiResponse<TeamCreateResponse>> createTeam(
            @PathVariable Long classId,
            @Valid @RequestBody TeamCreateRequest request,
            @AuthenticationPrincipal Long teacherId
    ) {
        TeamCreateResponse response = teamService.createTeam(classId, request, teacherId);
        return ResponseEntity.ok(new CommonApiResponse<>(true, "팀 생성 성공", response));
    }

    // 클리어
    @PostMapping("/{classId}/teams/{teamId}/assign")
    public ResponseEntity<CommonApiResponse<TeamAssignResponse>> assignStudentsToTeam(
            @PathVariable Long classId,
            @PathVariable Long teamId,
            @Valid @RequestBody TeamAssignRequest request,
            @AuthenticationPrincipal Long teacherId
    ) {
        TeamAssignResponse response = teamService.assignStudentsToTeam(classId, teamId, request, teacherId);
        return ResponseEntity.ok(new CommonApiResponse<>(true, "학생 팀 배정 성공", response));
    }

    // 클리어
    @PostMapping("/{classId}/teams/assign")
    public ResponseEntity<CommonApiResponse<TeamAutoAssignResponse>> autoAssignStudentsToTeams(
            @PathVariable Long classId,
            @AuthenticationPrincipal Long teacherId
    ) {
        TeamAutoAssignResponse response = teamService.autoAssignStudentsToTeams(classId, teacherId);
        return ResponseEntity.ok(new CommonApiResponse<>(true, "학생 자동 배정 성공", response));
    }

    @DeleteMapping("/{classId}/teams/{teamId}")
    public ResponseEntity<CommonApiResponse<TeamDeleteResponse>> deleteTeam(
            @PathVariable Long classId,
            @PathVariable Long teamId,
            @AuthenticationPrincipal Long teacherId
    ) {
        TeamDeleteResponse response = teamService.deleteTeam(classId, teamId, teacherId);
        return ResponseEntity.ok(new CommonApiResponse<>(true, "팀 삭제 성공", response));
    }
}
