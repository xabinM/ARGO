package com.argo.backend.organization.controller;

import com.argo.backend.organization.dto.classroomcreate.ClassCreateRequest;
import com.argo.backend.organization.dto.classroomcreate.ClassCreateResponse;
import com.argo.backend.organization.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teacher/classes")
public class TeacherClassController {

    private final ClassService classService;

    @PostMapping
    public ResponseEntity<ClassCreateResponse> createClass(
            @Valid @RequestBody ClassCreateRequest request,
            @AuthenticationPrincipal Long teacherId
    ) {
        ClassCreateResponse response = classService.createClass(teacherId, request);
        return ResponseEntity.ok(response);
    }

}

