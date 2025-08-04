package com.argo.backend.mission.controller;

import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.mission.dto.problemRegister.ProblemRegisterRequest;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateRequestFromCli;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateResponse;
import com.argo.backend.mission.dto.problemRegister.ProblemRegisterResponse;
import com.argo.backend.mission.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


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
}
