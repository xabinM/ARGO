package com.argo.backend.organization.controller;

/**
 * 반(ClassRoom) 관련 API 요청을 처리하는 컨트롤러
 * 반 생성, 조회, 수정, 삭제 등의 REST API 엔드포인트를 제공
 */

import com.argo.backend.organization.dto.CommonApiResponse;
import com.argo.backend.organization.dto.ClassStatusFilter;
import com.argo.backend.organization.dto.applicationlist.ApplicationListRequest;
import com.argo.backend.organization.dto.applicationlist.ApplicationListResponse;
import com.argo.backend.organization.dto.applicationprocess.ApplicationProcessRequest;
import com.argo.backend.organization.dto.applicationprocess.ApplicationProcessResponse;
import com.argo.backend.organization.dto.classapply.ClassApplyRequest;
import com.argo.backend.organization.dto.classapply.ClassApplyResponse;
import com.argo.backend.organization.dto.classdelete.ClassDeleteResponse;
import com.argo.backend.organization.dto.classdetail.ClassDetailResponse;
import com.argo.backend.organization.dto.classleave.ClassLeaveResponse;
import com.argo.backend.organization.dto.classroomcreate.ClassCreateRequest;
import com.argo.backend.organization.dto.classroomcreate.ClassCreateResponse;
import com.argo.backend.organization.dto.classroomlist.ClassListRequestDto;
import com.argo.backend.organization.dto.classroomlist.ClassListWithPaginationDto;
import com.argo.backend.organization.dto.randomassign.RandomAssignRequest;
import com.argo.backend.organization.dto.randomassign.RandomAssignResponse;
import com.argo.backend.organization.dto.studentlist.StudentListRequest;
import com.argo.backend.organization.dto.studentlist.StudentListResponse;
import com.argo.backend.organization.dto.teamassign.TeamAssignRequest;
import com.argo.backend.organization.dto.teamassign.TeamAssignResponse;
import com.argo.backend.organization.dto.teamcreate.TeamCreateRequest;
import com.argo.backend.organization.dto.teamcreate.TeamCreateResponse;
import com.argo.backend.organization.dto.teamdelete.TeamDeleteResponse;
import com.argo.backend.organization.service.*;
import com.argo.backend.organization.service.exception.AccessDeniedException;
import com.argo.backend.organization.service.exception.ClassNotFoundException;
import com.argo.backend.organization.service.exception.TeacherNotAllowedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/classes")
public class ClassRoomController {

    private final ClassRoomCreateService classRoomCreateService;
    private final ClassListService classListService;
    private final ClassDetailService classDetailService;
    private final ClassApplicationService classApplicationService;
    private final ApplicationListService applicationListService;
    private final ApplicationProcessService applicationProcessService;
    private final TeamCreateService teamCreateService;
    private final StudentListService studentListService;
    private final TeamAssignService teamAssignService;
    private final RandomAssignService randomAssignService;
    private final TeamDeleteService teamDeleteService;
    private final ClassDeleteService classDeleteService;
    private final ClassLeaveService classLeaveService;


    @PostMapping("/create")
    public ResponseEntity<CommonApiResponse<ClassCreateResponse>> createClass(
            @RequestBody ClassCreateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        try {
            // User-Id 헤더 검증
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "User-Id 헤더가 필요합니다."));
            }

            // 반 생성 서비스 호출
            ClassCreateResponse response = classRoomCreateService.createClass(userId, request);

            log.info("반 생성 성공 - classId: {}, teacherId: {}", response.classId(), userId);

            return ResponseEntity.ok()
                    .body(new CommonApiResponse<>(true, "반이 생성되었습니다.", response));

        } catch (SecurityException e) {
            log.warn("반 생성 권한 오류 - message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));

        } catch (IllegalArgumentException e) {
            log.warn("반 생성 입력값 오류 - message: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new CommonApiResponse<>(false, e.getMessage()));

        } catch (Exception e) {
            log.error("반 생성 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(false, "서버 내부 오류가 발생했습니다."));
        }
    }


    @GetMapping("/")
    public ResponseEntity<CommonApiResponse<ClassListWithPaginationDto>> getClassList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "active")  String status,
            @AuthenticationPrincipal Long userId)
    {
        try {
            // User-Id 헤더 검증
            // jwt 사용 시 삭제 부분
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "User-Id 헤더가 필요합니다."));
            }

            // 파라미터 검증
            if (page < 1) {
                return ResponseEntity.badRequest()
                        .body(new CommonApiResponse<>(false, "페이지 번호는 1 이상이어야 합니다."));
            }

            if (size < 1 || size > 100) {
                return ResponseEntity.badRequest()
                        .body(new CommonApiResponse<>(false, "페이지 크기는 1-100 사이여야 합니다."));
            }

            // status 파라미터 검증
            if (!ClassStatusFilter.isValid(status)) {
                return ResponseEntity.badRequest()
                        .body(new CommonApiResponse<>(false, 
                            "status는 " + String.join(", ", ClassStatusFilter.getValidValues()) + " 중 하나여야 합니다."));
            }

            // 요청 DTO 생성
            ClassListRequestDto request = ClassListRequestDto.builder()
                    .pageNum(page)
                    .pageSize(size)
                    .status(status)
                    .build();

            // 반 목록 조회
            ClassListWithPaginationDto result = classListService.findByList(userId, request);

            log.info("반 목록 조회 성공 - userId: {}, page: {}, size: {}, status: {}", 
                    userId, page, size, status);

            return ResponseEntity.ok(new CommonApiResponse<>(true, "반 목록 조회 성공", result));

        } catch (IllegalArgumentException e) {
            log.warn("반 목록 조회 입력값 오류 - message: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new CommonApiResponse<>(false, e.getMessage()));

        } catch (Exception e) {
            log.error("반 목록 조회 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(false, "서버 내부 오류가 발생했습니다."));
        }
    }


    @GetMapping("/{classId}")
    public ResponseEntity<CommonApiResponse<ClassDetailResponse>> getClassDetailById(
            @PathVariable Long classId,
            @AuthenticationPrincipal Long userId) {

        try {
            // User-Id 헤더 검증
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "User-Id 헤더가 필요합니다."));
            }

            // classId 검증
            if (classId == null || classId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "올바르지 않은 반 ID입니다."));
            }

            // 반 상세정보 조회
            ClassDetailResponse classDetailResponse = classDetailService.getClassDetail(classId, userId);

            return ResponseEntity.ok(new CommonApiResponse<>(true, "반 상세정보 조회 성공", classDetailResponse));

        } catch (ClassNotFoundException e) {
            // 존재하지 않는 반
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (AccessDeniedException e) {
            // 권한 없음
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (IllegalArgumentException e) {
            // 기타 입력값 오류
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            log.error("반 상세정보 조회 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(false, "서버 내부 오류가 발생했습니다."));
        }
    }


    @PostMapping("/apply")
    public ResponseEntity<CommonApiResponse<ClassApplyResponse>> applyToClass(
            @RequestBody ClassApplyRequest request,
            @AuthenticationPrincipal Long userId) {
        
        try {
            // User-Id 헤더 검증
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "User-Id 헤더가 필요합니다."));
            }

            // 반 참여 신청 서비스 호출
            ClassApplyResponse response = classApplicationService.applyToClass(userId, request);

            log.info("반 참여 신청 완료 - userId: {}, classId: {}, applicationId: {}", 
                    userId, response.getClassId(), response.getApplicationId());

            return ResponseEntity.ok()
                    .body(new CommonApiResponse<>(true, "반 참여 신청이 완료되었습니다. 선생님의 승인을 기다려주세요.", response));

        } catch (TeacherNotAllowedException e) {
            // 선생님 권한으로 신청 시도 - 403 Forbidden
            log.warn("반 참여 신청 실패 - 권한 오류: userId: {}", userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (IllegalArgumentException e) {
            // 기타 입력값 오류 (잘못된 초대 코드, 중복 신청 등) - 400 Bad Request
            log.warn("반 참여 신청 실패 - userId: {}, message: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            log.error("반 참여 신청 중 예상치 못한 오류 발생 - userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(false, "서버 내부 오류가 발생했습니다."));
        }
    }



    @GetMapping("/{classId}/applications")
    public ResponseEntity<CommonApiResponse<ApplicationListResponse>> getApplicationList(
            @PathVariable Long classId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(name = "status", required = false, defaultValue = "pending") String status,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size) {

        try {
            // User-Id 헤더 검증
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "User-Id 헤더가 필요합니다."));
            }

            // classId 검증
            if (classId == null || classId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "올바르지 않은 반 ID입니다."));
            }

            // 요청 DTO 생성
            ApplicationListRequest request = ApplicationListRequest.builder()
                    .status(status)
                    .page(page)
                    .size(size)
                    .build();

            // 신청 목록 조회
            ApplicationListResponse response = applicationListService.getApplicationList(classId, userId, request);

            log.info("참여 신청 목록 조회 성공 - userId: {}, classId: {}, status: {}, page: {}", 
                    userId, classId, status, page);

            return ResponseEntity.ok(new CommonApiResponse<>(true, "참여 신청 목록 조회 성공", response));

        } catch (ClassNotFoundException e) {
            // 존재하지 않는 반
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (AccessDeniedException e) {
            // 권한 없음
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (TeacherNotAllowedException e) {
            // 선생님 권한 필요
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (IllegalArgumentException e) {
            // 입력값 오류 (잘못된 상태값 등)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            log.error("참여 신청 목록 조회 중 예상치 못한 오류 발생 - userId: {}, classId: {}", userId, classId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(false, "서버 내부 오류가 발생했습니다."));
        }
    }

    @PutMapping("/{classId}/applications/{applicationId}")
    public ResponseEntity<CommonApiResponse<ApplicationProcessResponse>> processApplication(
            @PathVariable Long classId,
            @PathVariable Long applicationId,
            @RequestBody ApplicationProcessRequest request,
            @AuthenticationPrincipal Long userId) {

        try {
            // User-Id 헤더 검증
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "User-Id 헤더가 필요합니다."));
            }

            // 경로 파라미터 검증
            if (classId == null || classId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "올바르지 않은 반 ID입니다."));
            }

            if (applicationId == null || applicationId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "올바르지 않은 신청 ID입니다."));
            }

            // 신청 처리
            ApplicationProcessResponse response = applicationProcessService
                    .processApplication(classId, applicationId, userId, request);

            String message = "approve".equals(request.getAction()) 
                    ? "참여 신청이 승인되었습니다." 
                    : "참여 신청이 거절되었습니다.";

            return ResponseEntity.ok(new CommonApiResponse<>(true, message, response));

        } catch (TeacherNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (ClassNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            log.error("참여 신청 처리 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(false, "서버 내부 오류가 발생했습니다."));
        }
    }


    @PostMapping("/{classId}/teams")
    public ResponseEntity<CommonApiResponse<TeamCreateResponse>> createTeam(
            @PathVariable Long classId,
            @RequestBody TeamCreateRequest request,
            @AuthenticationPrincipal Long userId) {

        try {
            // User-Id 헤더 검증
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "User-Id 헤더가 필요합니다."));
            }

            if (classId == null || classId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "올바르지 않은 반 ID입니다."));
            }

            TeamCreateResponse response = teamCreateService.createTeam(classId, userId, request);
            return ResponseEntity.ok(new CommonApiResponse<>(true, "팀이 생성되었습니다.", response));

        } catch (TeacherNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (ClassNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            log.error("팀 생성 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(false, "서버 내부 오류가 발생했습니다."));
        }
    }


    @GetMapping("/{classId}/students")
    public ResponseEntity<CommonApiResponse<StudentListResponse>> getStudentList(
            @PathVariable Long classId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(name = "status", required = false, defaultValue = "all") String status,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size)
    {

        try {
            // User-Id 헤더 검증
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "User-Id 헤더가 필요합니다."));
            }

            if (classId == null || classId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "올바르지 않은 반 ID입니다."));
            }

            StudentListRequest request = StudentListRequest.builder()
                    .status(status)
                    .page(page)
                    .size(size)
                    .build();

            StudentListResponse response = studentListService.getStudentList(classId, userId, request);
            return ResponseEntity.ok(new CommonApiResponse<>(true, "학생 목록 조회 성공", response));

        } catch (TeacherNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (ClassNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            log.error("학생 목록 조회 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(false, "서버 내부 오류가 발생했습니다."));
        }
    }


    @PostMapping("/{classId}/teams/{teamId}/assign")
    public ResponseEntity<CommonApiResponse<TeamAssignResponse>> assignStudentsToTeam(
            @PathVariable Long classId,
            @PathVariable Long teamId,
            @RequestBody TeamAssignRequest request,
            @AuthenticationPrincipal Long userId) {

        try {
            // User-Id 헤더 검증
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "User-Id 헤더가 필요합니다."));
            }

            if (classId == null || classId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "올바르지 않은 반 ID입니다."));
            }

            if (teamId == null || teamId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new CommonApiResponse<>(false, "올바르지 않은 팀 ID입니다."));
            }

            TeamAssignResponse response = teamAssignService
                    .assignStudentsToTeam(classId, teamId, userId, request);

            String message = request.getStudentCount() + "명의 학생이 팀에 배정되었습니다.";
            return ResponseEntity.ok(new CommonApiResponse<>(true, message, response));

        } catch (TeacherNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (ClassNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            log.error("팀 배정 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CommonApiResponse<>(false, "서버 내부 오류가 발생했습니다."));
        }
    }

    @PostMapping("/{classId}/teams/random-assign")
    public ResponseEntity<CommonApiResponse<RandomAssignResponse>> randomAssignStudents(
            @PathVariable Long classId,
            @RequestBody(required = false) RandomAssignRequest request, // balanced / random
            @AuthenticationPrincipal Long userId) {
        try {
            if (request == null) request = RandomAssignRequest.builder().build();
            //  빈 RandomAssignRequest 객체를 새로 생성해서 할당

            RandomAssignResponse response = randomAssignService.randomAssignStudents(classId, userId, request);
            String message = response.getTotalAssigned() + "명의 학생이 " + 
                           response.getTeamAssignments().size() + "개 팀에 랜덤 배정되었습니다.";
            return ResponseEntity.ok(new CommonApiResponse<>(true, message, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{classId}/teams/{teamId}")
    public ResponseEntity<CommonApiResponse<TeamDeleteResponse>> deleteTeam(
            @PathVariable Long classId,
            @PathVariable Long teamId,
            @AuthenticationPrincipal Long userId) {
        try {
            TeamDeleteResponse response = teamDeleteService.deleteTeam(classId, teamId, userId);
            String message = response.getUnassignedStudents().isEmpty() ? 
                    "팀이 삭제되었습니다." : 
                    "팀이 삭제되었습니다. " + response.getUnassignedStudents().size() + "명의 학생이 팀 미배정 상태가 되었습니다.";
            return ResponseEntity.ok(new CommonApiResponse<>(true, message, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{classId}")
    public ResponseEntity<CommonApiResponse<ClassDeleteResponse>> deleteClass(
            @PathVariable Long classId,
            @AuthenticationPrincipal Long userId) {
        try {
            ClassDeleteResponse response = classDeleteService.deleteClass(classId, userId);
            return ResponseEntity.ok(new CommonApiResponse<>(true, "반이 삭제되었습니다. 관련된 모든 데이터가 함께 삭제되었습니다.", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{classId}/leave")
    public ResponseEntity<CommonApiResponse<ClassLeaveResponse>> leaveClass(
            @PathVariable Long classId,
            @AuthenticationPrincipal Long userId) {
        try {
            ClassLeaveResponse response = classLeaveService.leaveClass(classId, userId);
            String message = response.getTeamInfo().getWasInTeam() ? 
                    "반에서 탈퇴했습니다. 팀에서도 자동으로 제거되었습니다." : 
                    "반에서 탈퇴했습니다.";
            return ResponseEntity.ok(new CommonApiResponse<>(true, message, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CommonApiResponse<>(false, e.getMessage()));
        }
    }




}