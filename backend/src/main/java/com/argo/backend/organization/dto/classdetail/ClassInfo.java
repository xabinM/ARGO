package com.argo.backend.organization.dto.classdetail;

import com.argo.backend.domain.classroom.ClassRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ClassInfo {
    private final Long classId;
    private final String className;
    private final String description;
    private final String location;
    private final LocalDate activityDate;
    private final Integer maxStudents;
    private final String status;
    private final String inviteCode;
    private final Long teacherId;
    private final String teacherName;
    private final LocalDateTime createdAt;

    // 선생님용 반 전체 정보
    public static ClassInfo fromForTeacher(ClassRoom classRoom, String location) {
        return ClassInfo.builder()
                .classId(classRoom.getClassId())
                .className(classRoom.getClassName())
                .description(classRoom.getDescription())
                .location(location)
                .activityDate(classRoom.getActivityDate())
                .maxStudents(classRoom.getMaxStudents())
                .status(classRoom.getStatus().name().toLowerCase())
                .inviteCode(classRoom.getInviteCode()) // 선생님만 초대코드 조회 가능
                .teacherId(classRoom.getTeacher().getUserId())
                .teacherName(classRoom.getTeacher().getName())
                .createdAt(classRoom.getCreatedAt())
                .build();
    }

    // 학생용 제한된 반 정보 (초대코드, teacherId, createdAt 제외)
    public static ClassInfo fromForStudent(ClassRoom classRoom, String location) {
        return ClassInfo.builder()
                .classId(classRoom.getClassId())
                .className(classRoom.getClassName())
                .description(classRoom.getDescription())
                .location(location)
                .activityDate(classRoom.getActivityDate())
                .maxStudents(classRoom.getMaxStudents())
                .status(classRoom.getStatus().name().toLowerCase())
                .teacherName(classRoom.getTeacher().getName())
                .build();
    }

    // 기존 호환성을 위한 메서드 유지
    public static ClassInfo from(ClassRoom classRoom, String location) {
        return fromForTeacher(classRoom, location);
    }
}

