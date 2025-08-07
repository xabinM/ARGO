package com.argo.backend.organization.dto.classdetail;

import com.argo.backend.domain.classroom.entity.ClassRoom;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ClassInfoDetailDto {
    
    private Long classId;
    private String className;
    private String description;
    private String location;
    private LocalDate activityDate;
    private int maxStudents;
    private String status;
    private String inviteCode;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime createdAt;
    
    public ClassInfoDetailDto(Long classId, String className, String description, String location,
                             LocalDate activityDate, int maxStudents, String status, String inviteCode,
                             Long teacherId, String teacherName, LocalDateTime createdAt) {
        this.classId = classId;
        this.className = className;
        this.description = description;
        this.location = location;
        this.activityDate = activityDate;
        this.maxStudents = maxStudents;
        this.status = status;
        this.inviteCode = inviteCode;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.createdAt = createdAt;
    }
    
    public static ClassInfoDetailDto fromTeacher(ClassRoom classRoom) {
        return new ClassInfoDetailDto(
                classRoom.getClassId(),
                classRoom.getClassName(),
                classRoom.getDescription(),
                classRoom.getLocation().getName(),
                classRoom.getActivityDate(),
                classRoom.getMaxStudents(),
                classRoom.getStatus().name().toLowerCase(),
                classRoom.getInviteCode(),
                classRoom.getTeacher().getUserId(),
                classRoom.getTeacher().getName(),
                classRoom.getCreatedAt()
        );
    }
    
    public static ClassInfoDetailDto fromStudent(ClassRoom classRoom) {
        return new ClassInfoDetailDto(
                classRoom.getClassId(),
                classRoom.getClassName(),
                classRoom.getDescription(),
                classRoom.getLocation().getName(),
                classRoom.getActivityDate(),
                classRoom.getMaxStudents(),
                classRoom.getStatus().name().toLowerCase(),
                null, // 학생에게는 초대코드 숨김
                null, // 학생에게는 선생님 ID 숨김
                classRoom.getTeacher().getName(),
                null // 학생에게는 생성일 숨김
        );
    }
}