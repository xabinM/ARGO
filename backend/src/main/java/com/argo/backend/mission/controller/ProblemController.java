package com.argo.backend.mission.controller;

import com.argo.backend.domain.ploblem.enums.PhotoPose;
import com.argo.backend.domain.ploblem.enums.ProblemType;
import com.argo.backend.global.enums.ResponseMessage;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateResponse;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateTransDto;
import com.argo.backend.mission.dto.problemRegister.ProblemRegisterRequest;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateRequestFromCli;
import com.argo.backend.mission.dto.problemRegister.ProblemRegisterResponse;
import com.argo.backend.mission.dto.problemsList.ProblemListPerTypeResponse;
import com.argo.backend.mission.dto.common.ProblemDetail;
import com.argo.backend.mission.dto.problemsList.AllProblemListResponse;
import com.argo.backend.mission.dto.selfieDetermine.SelfieRequestDto;
import com.argo.backend.mission.dto.selfieDetermine.SelfieResultDto;
import com.argo.backend.mission.dto.selfieDetermine.SelfieResultResponse;
import com.argo.backend.mission.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problem")
public class ProblemController {

    private final ProblemService problemService;

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/register/spot/{spotId}")
    public ResponseEntity<?> registerProblem(@PathVariable Long spotId,
                                             @RequestBody @Valid ProblemRegisterRequest request) {

        problemService.registerQuizProblem(spotId, request);
        return ResponseEntity.ok(new ProblemRegisterResponse(true,
                ResponseMessage.SUCCESS_REGISTER_PROBLEM.getMessage())
        );
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/generate")
    public ResponseEntity<?> generateProblem(@RequestBody ProblemGenerateRequestFromCli request) {
        ProblemGenerateTransDto dto = problemService.generateProblem(request);

        return ResponseEntity.ok(new ProblemGenerateResponse(
                        true, ResponseMessage.SUCCESS_GENERATE_PROBLEM.getMessage(), dto.getGrade(),
                        dto.getSpotName(), dto.getProblems()
                )
        );
    }

    @PostMapping("/test/quiz")
    public ResponseEntity<Map<String, Object>> testQuizGeneration() {
        try {
            Map<String, Object> result = problemService.testQuizGeneration("경복궁", 3, 2);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/spot/{spotId}")
    public ResponseEntity<?> getProblemsBySpotId(@PathVariable Long spotId) {

        List<ProblemDetail> problems = problemService.getProblemsBySpotId(spotId);

        return ResponseEntity.ok(new AllProblemListResponse(true, problems));
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/spot/{spotId}/perType")
    public ResponseEntity<ProblemListPerTypeResponse> getProblemsBySpotAndType(
            @PathVariable Long spotId,
            @RequestParam ProblemType type) {

        List<ProblemDetail> problems = problemService.findProblemsBySpotIdAndType(spotId, type);
        return ResponseEntity.ok(new ProblemListPerTypeResponse(true, problems));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping(value = "/selfie/determine/team/{teamId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> determineSelfiePose(@PathVariable Long teamId,
                                                 @RequestParam("image") MultipartFile imageFile,
                                                 @RequestParam("pose") PhotoPose pose) throws IOException {
        SelfieResultDto result = problemService.determineSelfie(new SelfieRequestDto(teamId, imageFile, pose));

        return ResponseEntity.ok(new SelfieResultResponse(true, result));
    }
}