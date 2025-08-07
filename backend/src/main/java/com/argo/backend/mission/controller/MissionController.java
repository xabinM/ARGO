package com.argo.backend.mission.controller;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.mission.dto.missionCreate.MissionCreateDto;
import com.argo.backend.mission.dto.missionCreate.MissionCreateResponse;
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

//
//    @GetMapping("/overview/team/{teamId}/")
//    public ResponseEntity<?>
//

//    // 미션 제출 (퀴즈)
//    @PostMapping("/{missionId}/submit/quiz")
//    public ResponseEntity<?> submitQuiz() {
//
//        return ResponseEntity.ok();
//    }
//    // 미션 제출 (셀카)
//    @PostMapping("/{missionId}/submit/selfie")
//    public ResponseEntity<?> submitSelfie() {
//
//        return ResponseEntity.ok();
//    }
}
