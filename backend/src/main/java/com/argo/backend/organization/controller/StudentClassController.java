package com.argo.backend.organization.controller;

import com.argo.backend.organization.dto.classapply.ClassApplyResponse;
import com.argo.backend.organization.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/classes")
public class StudentClassController {

    private final ClassService classService;

    @PostMapping("/apply")
    public ResponseEntity<ClassApplyResponse> applyClass(
            @RequestParam String inviteCode,
            @AuthenticationPrincipal Long studentId
    ) {
        ClassApplyResponse response = classService.applyToClass(studentId, inviteCode);
        return ResponseEntity.ok(response);
    }
}
