package com.argo.backend.organization.dto.classlist;

import com.argo.backend.domain.classroom.entity.ClassRoom;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ClassInfoDto {
    
    private Long classId;
    private String className;
    private String description;
    private String location;
    private LocalDate activityDate;
    private int studentCount;
    private int maxStudents;
    private int teamCount;
    private String status;
    private String inviteCode;
    private LocalDateTime createdAt;
    
    public ClassInfoDto(Long classId, String className, String description, String location, 
                       LocalDate activityDate, int studentCount, int maxStudents, int teamCount, 
                       String status, String inviteCode, LocalDateTime createdAt) {
        this.classId = classId;
        this.className = className;
        this.description = description;
        this.location = location;
        this.activityDate = activityDate;
        this.studentCount = studentCount;
        this.maxStudents = maxStudents;
        this.teamCount = teamCount;
        this.status = status;
        this.inviteCode = inviteCode;
        this.createdAt = createdAt;
    }
    
    public static ClassInfoDto from(ClassRoom classRoom, int studentCount, int teamCount) {
        return new ClassInfoDto(
                classRoom.getClassId(),
                classRoom.getClassName(),
                classRoom.getDescription(),
                classRoom.getLocation().getName(),
                classRoom.getActivityDate(),
                studentCount,
                classRoom.getMaxStudents(),
                teamCount,
                classRoom.getStatus().name().toLowerCase(),
                classRoom.getInviteCode(),
                classRoom.getCreatedAt()
        );
    }
    
    public static ClassInfoDto fromStudent(ClassRoom classRoom, int studentCount, int teamCount) {
        return new ClassInfoDto(
                classRoom.getClassId(),
                classRoom.getClassName(),
                classRoom.getDescription(),
                classRoom.getLocation().getName(),
                classRoom.getActivityDate(),
                studentCount,
                classRoom.getMaxStudents(),
                teamCount,
                classRoom.getStatus().name().toLowerCase(),
                null, // 학생에게는 초대코드 숨김
                classRoom.getCreatedAt()
        );
    }
}