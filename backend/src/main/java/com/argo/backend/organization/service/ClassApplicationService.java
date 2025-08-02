package com.argo.backend.organization.service;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.organization.dto.applicationlist.*;
import com.argo.backend.organization.exception.ClassNotFoundException;
import com.argo.backend.organization.exception.UnauthorizedClassAccessException;
import com.argo.backend.organization.repository.ClassApplicationRepository;
import com.argo.backend.organization.repository.ClassRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassApplicationService {

    private final ClassApplicationRepository classApplicationRepository;
    private final ClassRoomRepository classRoomRepository;

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

        // Query 결과가 단일 행이므로 첫 번째 요소가 Object[] 배열
        Object[] statistics = (Object[]) result[0];
        
        Long totalApplications = ((Number) statistics[0]).longValue();
        Long pendingCount = ((Number) statistics[1]).longValue();
        Long approvedCount = ((Number) statistics[2]).longValue();
        Long rejectedCount = ((Number) statistics[3]).longValue();
        
        return StatisticsDto.of(totalApplications, pendingCount, approvedCount, rejectedCount);
    }
}
