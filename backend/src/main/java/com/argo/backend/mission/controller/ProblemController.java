package com.argo.backend.mission.controller;

import com.argo.backend.domain.ploblem.enums.ProblemType;
import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.mission.dto.problemRegister.ProblemRegisterRequest;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateRequestFromCli;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateResponse;
import com.argo.backend.mission.dto.problemRegister.ProblemRegisterResponse;
import com.argo.backend.mission.dto.problemsList.ProblemListPerTypeResponse;
import com.argo.backend.mission.dto.common.ProblemResponseDto;
import com.argo.backend.mission.dto.problemsList.AllProblemListResponse;
import com.argo.backend.mission.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problem")
@PreAuthorize("hasRole('TEACHER')")
public class ProblemController {

    private final ProblemService problemService;

    @PostMapping("/register/spot/{spotId}")
    public ResponseEntity<?> registerProblem(@PathVariable Long spotId, @RequestBody @Valid ProblemRegisterRequest request) {

        problemService.registerQuizProblem(spotId, request);
        return ResponseEntity.ok(new ProblemRegisterResponse(ResponseMessage.SUCCESS_REGISTER_PROBLEM.getMessage()));
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateProblem(@RequestBody ProblemGenerateRequestFromCli request) {
       ProblemGenerateResponse response = problemService.generateProblem(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/spot/{spotId}")
    public ResponseEntity<?> getProblemsBySpotId(@PathVariable Long spotId) {

        List<ProblemResponseDto> dto = problemService.getProblemsBySpotId(spotId);

        return ResponseEntity.ok(new AllProblemListResponse(dto));
    }

    @GetMapping("/spot/{spotId}/perType")
    public ResponseEntity<ProblemListPerTypeResponse> getProblemsBySpotAndType(
            @PathVariable Long spotId,
            @RequestParam ProblemType type) {

        List<ProblemResponseDto> dto = problemService.findProblemsBySpotIdAndType(spotId, type);
        return ResponseEntity.ok(new ProblemListPerTypeResponse(dto));
    }
}
