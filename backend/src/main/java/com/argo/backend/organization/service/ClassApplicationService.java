package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.entity.ClassApplication;
import com.argo.backend.domain.classroom.entity.ClassRoom;
import com.argo.backend.domain.classroom.enums.ApplicationStatus;
import com.argo.backend.organization.dto.applicationlist.*;
import com.argo.backend.organization.dto.applicationprocess.*;
import com.argo.backend.organization.exception.types.*;
import com.argo.backend.organization.exception.types.ClassNotFoundException;
import com.argo.backend.domain.classroom.repository.ClassApplicationRepository;
import com.argo.backend.domain.classroom.repository.ClassRoomRepository;
import com.argo.backend.redis.logic.GpsRedis;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassApplicationService {

    private final ClassApplicationRepository classApplicationRepository;
    private final ClassRoomRepository classRoomRepository;
    private final GpsRedis gpsRedis;

    // 신청 리스트 받기
    @Transactional
    public ApplicationListResponse getApplicationList(Long classId, String status, Pageable pageable, Long teacherId) {

        ClassRoom classRoom = validateClassAccess(classId, teacherId);
        
        Page<ClassApplication> applicationsPage = getApplicationsByStatus(classId, status, pageable);
        
        StatisticsDto statistics = createStatistics(classId);
        
        ClassInfoDto classInfo = ClassInfoDto.from(classRoom, statistics.approvedCount());
        PaginationDto pagination = PaginationDto.from(applicationsPage);
        List<ApplicationDto> applications = applicationsPage.getContent().stream()
                .map(ApplicationDto::from)
                .toList();
        
        return new ApplicationListResponse(classInfo, applications, statistics, pagination);
    }


    // 신청 처리하기
    @Transactional
    public ApplicationProcessResponse processApplications(Long classId, ApplicationProcessRequest request, Long teacherId) {
        ClassRoom classRoom = validateClassAccess(classId, teacherId);
        
        List<ClassApplication> applications = findAndValidateApplications(classId, request.getApplicationIds());
        
        if ("approve".equals(request.getAction())) {
            return processApproval(classRoom, applications);
        } else {
            return processRejection(applications);
        }
    }



    private ClassRoom validateClassAccess(Long classId, Long teacherId) {
        ClassRoom classRoom = classRoomRepository.findById(classId)
                .orElseThrow(ClassNotFoundException::new);

        if (!classRoom.getTeacher().getUserId().equals(teacherId)) {
            throw new UnauthorizedClassAccessException();
        }

        return classRoom;
    }

    private Page<ClassApplication> getApplicationsByStatus(Long classId, String status, Pageable pageable) {
        if ("ALL".equals(status)) {
            return classApplicationRepository.findByClassRoomClassId(classId, pageable);
        }

        ApplicationStatus applicationStatus = ApplicationStatus.valueOf(status.toUpperCase());
        return classApplicationRepository.findByClassRoomClassIdAndStatus(classId, applicationStatus, pageable);
    }

    private StatisticsDto createStatistics(Long classId) {
        Object[] result = classApplicationRepository.findStatisticsByClassId(classId);

        if (result == null || result.length == 0) {
            return StatisticsDto.of(0L, 0L, 0L, 0L);
        }

        Object[] statistics = (Object[]) result[0];

        Long totalApplications = ((Number) statistics[0]).longValue();
        Long pendingCount = ((Number) statistics[1]).longValue();
        Long approvedCount = ((Number) statistics[2]).longValue();
        Long rejectedCount = ((Number) statistics[3]).longValue();

        return StatisticsDto.of(totalApplications, pendingCount, approvedCount, rejectedCount);
    }


    private List<ClassApplication> findAndValidateApplications(Long classId, List<Long> applicationIds) {
        List<ClassApplication> applications = classApplicationRepository.findByApplicationIdsAndClassId(applicationIds, classId);
        
        // 존재 검증
        if (applications.size() != applicationIds.size()) {
            throw new ApplicationNotFoundException();
        }
        
        // 상태 검증
        for (ClassApplication application : applications) {
            if (application.getStatus() != ApplicationStatus.PENDING) {
                throw new ApplicationAlreadyProcessedException();
            }
        }
        
        return applications;
    }
    
    private ApplicationProcessResponse processApproval(ClassRoom classRoom, List<ClassApplication> applications) {
        validateClassCapacityForApproval(classRoom, applications.size());
        
        LocalDateTime processedAt = LocalDateTime.now();
        List<ApplicationProcessResultDto> results = applications.stream()
                .map(application -> {
                    application.setStatus(ApplicationStatus.APPROVED);
                    application.setProcessedAt(processedAt);

                    // ✅ Redis 갱신: 전체 목록 조회 후 덮어쓰기
                    Long userId = application.getUser().getUserId();
                    List<Long> classIds = classApplicationRepository
                            .findAllByUser_UserIdAndStatus(userId, ApplicationStatus.APPROVED)
                            .stream()
                            .map(app -> app.getClassRoom().getClassId())
                            .toList();
                    gpsRedis.setUserClassIds(userId, classIds);

                    return ApplicationProcessResultDto.from(application);
                })
                .toList();
        
        return ApplicationProcessResponse.from(results);
    }
    
    private ApplicationProcessResponse processRejection(List<ClassApplication> applications) {
        LocalDateTime processedAt = LocalDateTime.now();
        List<ApplicationProcessResultDto> results = applications.stream()
                .map(application -> {
                    application.setStatus(ApplicationStatus.REJECTED);
                    application.setProcessedAt(processedAt);
                    return ApplicationProcessResultDto.from(application);
                })
                .toList();
        
        return ApplicationProcessResponse.from(results);
    }
    
    private void validateClassCapacityForApproval(ClassRoom classRoom, int approvalCount) {
        StatisticsDto statistics = createStatistics(classRoom.getClassId());
        long currentApproved = statistics.approvedCount();
        long maxStudents = classRoom.getMaxStudents();
        
        if (currentApproved + approvalCount > maxStudents) {
            throw new ClassCapacityExceededException();
        }
    }


}
