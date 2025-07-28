package com.argo.backend.organization.dto.classleave;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.user.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassStatusInfo {
    private final int totalStudents;
    private final int maxStudents;
    private final int availableSlots;

    public static ClassStatusInfo from(ClassRoom classRoom) {
        int approvedCount = (int) classRoom.getApplications().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .count();
        
        // 탈퇴한 학생 제외 (실제로는 이미 삭제되어서 approvedCount가 탈퇴 후 수치)
        int currentStudents = approvedCount;
        Integer maxStudents = classRoom.getMaxStudents();
        int max = maxStudents != null ? maxStudents : 999; // 기본값 설정
        
        return ClassStatusInfo.builder()
                .totalStudents(currentStudents)
                .maxStudents(max)
                .availableSlots(max - currentStudents)
                .build();
    }
}