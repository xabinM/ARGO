package com.argo.backend.organization.controller;

import com.argo.backend.organization.dto.classapply.ClassApplyResponse;
import com.argo.backend.organization.dto.classlist.ClassListResponse;
import com.argo.backend.organization.dto.classdetail.ClassDetailResponse;
import com.argo.backend.organization.service.ClassService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/classes")
@Validated
public class StudentClassController {

    private final ClassService classService;

    @GetMapping
    public ResponseEntity<ClassListResponse> getClassList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", defaultValue = "active")
            @Pattern(regexp = "^(active|inactive|all)$", message = "상태는 active, inactive, all 중 하나여야 합니다")
            String status,
            @AuthenticationPrincipal Long studentId
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.unsorted());
        ClassListResponse response = classService.getStudentClassList(studentId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{classId}")
    public ResponseEntity<ClassDetailResponse> getClassDetail(
            @PathVariable Long classId,
            @AuthenticationPrincipal Long studentId
    ) {
        ClassDetailResponse response = classService.getStudentClassDetail(studentId, classId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/apply")
    public ResponseEntity<ClassApplyResponse> applyClass(
            @RequestParam String inviteCode,
            @AuthenticationPrincipal Long studentId
    ) {
        ClassApplyResponse response = classService.applyToClass(studentId, inviteCode);
        return ResponseEntity.ok(response);
    }
}
