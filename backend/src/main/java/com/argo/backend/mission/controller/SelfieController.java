package com.argo.backend.mission.controller;

import com.argo.backend.mission.api.PythonApiClient;
import com.argo.backend.mission.dto.selfieDetermine.SelfieResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/selfie")
public class SelfieController {

    private final PythonApiClient pythonApiClient;

    @PostMapping("/determine")
    public ResponseEntity<?> determineSelfiePose(@RequestParam("image") MultipartFile imageFile) throws IOException {
        SelfieResultResponse result = pythonApiClient.requestDeterMineSelfie(imageFile);

        return ResponseEntity.ok(result);
    }
}
