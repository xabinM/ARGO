package com.argo.backend.organization.controller;

import com.argo.backend.organization.dto.teamcreate.TeamCreateRequest;
import com.argo.backend.organization.dto.teamcreate.TeamCreateResponse;
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
    public ResponseEntity<TeamCreateResponse> createTeam(
            @PathVariable Long classId,
            @Valid @RequestBody TeamCreateRequest request,
            @AuthenticationPrincipal Long teacherId
    ) {
        TeamCreateResponse response = teamService.createTeam(classId, request, teacherId);
        return ResponseEntity.ok(response);
    }
}
