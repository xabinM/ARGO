package com.argo.backend.mission.controller;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.mission.dto.SubmitMission.MissionSubmitDto;
import com.argo.backend.mission.dto.SubmitMission.MissionSubmitResponse;
import com.argo.backend.mission.dto.missionCreate.MissionCreateDto;
import com.argo.backend.mission.dto.missionCreate.MissionCreateResponse;
import com.argo.backend.mission.dto.missionSubmit.MissionSubmitRequest;
import com.argo.backend.mission.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    @PostMapping("/create/team/{teamId}/spot/{spotId}")
    public ResponseEntity<?> createMission(@PathVariable Long teamId,
                                           @PathVariable Long spotId
    ) {
        MissionCreateDto dto = missionService.createMission(teamId, spotId);

        return ResponseEntity.ok(new MissionCreateResponse(true,
                ResponseMessage.SUCCESS_CREATE_MISSION.getMessage(),
                dto.getMissionId(), dto.getProblemDetail())
        );
    }

    // 미션 제출 (Card 배정)
    @PostMapping("/{missionId}/submit")
    public ResponseEntity<?> submitMission(@PathVariable Long missionId,
                                           @RequestBody MissionSubmitRequest request) {

        MissionSubmitDto dto = missionService.submitMission(missionId, request.isSuccess());

        return ResponseEntity.ok(new MissionSubmitResponse(dto.successful(),
                ResponseMessage.SUCCESS_SUBMIT_MISSION.getMessage(),
                dto.cardId(), dto.tier())
        );
    }
//
//    @GetMapping("/overview/team/{teamId}/")
//    public ResponseEntity<?>
//
}
