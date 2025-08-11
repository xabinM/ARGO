package com.argo.backend.organization.controller;

import com.argo.backend.organization.dto.CommonApiResponse;
import com.argo.backend.organization.dto.classroomcreate.ClassCreateRequest;
import com.argo.backend.organization.dto.classroomcreate.ClassCreateResponse;
import com.argo.backend.organization.dto.classdelete.ClassDeleteResponse;
import com.argo.backend.organization.dto.applicationlist.ApplicationListResponse;
import com.argo.backend.organization.dto.applicationprocess.ApplicationProcessRequest;
import com.argo.backend.organization.dto.applicationprocess.ApplicationProcessResponse;
import com.argo.backend.organization.dto.classlist.ClassListResponse;
import com.argo.backend.organization.dto.classdetail.ClassDetailResponse;
import com.argo.backend.organization.dto.location.LocationsResponse;
import com.argo.backend.organization.dto.spot.SpotsResponse;
import com.argo.backend.organization.dto.studentlist.StudentListResponse;
import com.argo.backend.organization.service.ClassService;
import com.argo.backend.organization.service.ClassApplicationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.argo.backend.organization.message.ResponseMessage;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teacher/classes")
@Validated
@PreAuthorize("hasRole('TEACHER')")
public class TeacherClassController {

    private final ClassService classService;
    private final ClassApplicationService classApplicationService;

    @GetMapping("/locations")
    public ResponseEntity<CommonApiResponse<List<LocationsResponse>>> getLocations() {
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.LOCATION_LIST_SUCCESS, classService.getLocations()));
    }

    @GetMapping("{classId}/spots")
    public ResponseEntity<CommonApiResponse<List<SpotsResponse>>> getSpots(
            @PathVariable Long classId,
            @AuthenticationPrincipal Long teacherId
    ) {
        List<SpotsResponse> spots = classService.getSpots(classId, teacherId);
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.SPOT_LIST_SUCCESS, spots));
    }

    @GetMapping
    public ResponseEntity<CommonApiResponse<ClassListResponse.ClassListData>> getClassList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", defaultValue = "active")
            @Pattern(regexp = "^(active|inactive|all)$", message = "상태는 active, inactive, all 중 하나여야 합니다")
            String status,
            @AuthenticationPrincipal Long teacherId
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.unsorted());
        ClassListResponse response = classService.getTeacherClassList(teacherId, status, pageable);
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.CLASS_LIST_SUCCESS, response.getData()));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    @GetMapping("/{classId}")
    public ResponseEntity<CommonApiResponse<ClassDetailResponse>> getClassDetail(
            @PathVariable Long classId,
            @RequestParam(value = "include", required = false) String include,
            @AuthenticationPrincipal Long userId
    ) {
        ClassDetailResponse response = classService.getClassDetail(userId, classId, include);
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.CLASS_DETAIL_SUCCESS, response));
    }

    @PostMapping
    public ResponseEntity<CommonApiResponse<ClassCreateResponse>> createClass(
            @Valid @RequestBody ClassCreateRequest request,
            @AuthenticationPrincipal Long teacherId
    ) {
        ClassCreateResponse response = classService.createClass(teacherId, request);
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.CLASS_CREATE_SUCCESS, response));
    }

    @GetMapping("/{classId}/applications")
    public ResponseEntity<CommonApiResponse<ApplicationListResponse>> getApplicationList(
            @PathVariable Long classId,
            @RequestParam(value = "status", defaultValue = "ALL")
            @Pattern(regexp = "^(PENDING|APPROVED|REJECTED|ALL)$", message = "상태는 PENDING, APPROVED, REJECTED, ALL 중 하나여야 합니다")
            String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Long teacherId
    ) {
        ApplicationListResponse response = classApplicationService.getApplicationList(classId, status, pageable, teacherId);
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.APPLICATION_LIST_SUCCESS, response));
    }

    @PutMapping("/{classId}/applications")
    public ResponseEntity<CommonApiResponse<ApplicationProcessResponse>> processApplications(
            @PathVariable Long classId,
            @Valid @RequestBody ApplicationProcessRequest request,
            @AuthenticationPrincipal Long teacherId
    ) {
        ApplicationProcessResponse response = classApplicationService.processApplications(classId, request, teacherId);
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.APPLICATION_PROCESS_SUCCESS, response));
    }

    @GetMapping("/{classId}/students")
    public ResponseEntity<CommonApiResponse<StudentListResponse>> getClassStudents(
            @PathVariable Long classId,
            @RequestParam(value = "status", defaultValue = "all") String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal Long teacherId
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        StudentListResponse response = classService.getClassStudents(teacherId, classId, status, pageable);
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.STUDENT_LIST_SUCCESS, response));
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<CommonApiResponse<ClassDeleteResponse>> deleteClass(
            @PathVariable Long classId,
            @AuthenticationPrincipal Long teacherId
    ) {
        ClassDeleteResponse response = classService.deleteClass(teacherId, classId);
        return ResponseEntity.ok(CommonApiResponse.success(ResponseMessage.CLASS_DELETE_SUCCESS, response));
    }
}

