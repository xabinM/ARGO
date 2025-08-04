package com.argo.backend.mission.controller;

import com.argo.backend.mission.dto.MissionCreate.MissionCreateDto;
import com.argo.backend.mission.dto.MissionCreate.MissionCreateResponse;
import com.argo.backend.mission.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    @PostMapping("/team/{teamId}/spot/{spotId}")
    public ResponseEntity<?> createMission(@PathVariable Long teamId,
                                           @PathVariable Long spotId
                                                                    ) {
        MissionCreateDto dto = missionService.createMission(teamId, spotId);

        return ResponseEntity.ok(new MissionCreateResponse("A", dto.getProblem()));
    }
}
