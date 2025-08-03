package com.argo.backend.organization.dto.classdelete;

import com.argo.backend.domain.classroom.ClassRoom;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class DeletedClassDto {
    
    private Long classId;
    private String className;
    private String description;
    private String location;
    private LocalDate activityDate;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    
    public DeletedClassDto(Long classId, String className, String description, String location,
                          LocalDate activityDate, Long teacherId, String teacherName,
                          LocalDateTime createdAt, LocalDateTime deletedAt) {
        this.classId = classId;
        this.className = className;
        this.description = description;
        this.location = location;
        this.activityDate = activityDate;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }
    
    public static DeletedClassDto from(ClassRoom classRoom, LocalDateTime deletedAt) {
        return new DeletedClassDto(
                classRoom.getClassId(),
                classRoom.getClassName(),
                classRoom.getDescription(),
                classRoom.getLocation().getName(),
                classRoom.getActivityDate(),
                classRoom.getTeacher().getUserId(),
                classRoom.getTeacher().getName(),
                classRoom.getCreatedAt(),
                deletedAt
        );
    }
}