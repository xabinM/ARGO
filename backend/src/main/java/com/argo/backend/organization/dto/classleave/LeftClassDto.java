package com.argo.backend.organization.dto.classleave;

import com.argo.backend.domain.classroom.entity.ClassRoom;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class LeftClassDto {
    
    private Long classId;
    private String className;
    private String description;
    private String location;
    private LocalDate activityDate;
    private String teacherName;
    
    public LeftClassDto(Long classId, String className, String description, 
                       String location, LocalDate activityDate, String teacherName) {
        this.classId = classId;
        this.className = className;
        this.description = description;
        this.location = location;
        this.activityDate = activityDate;
        this.teacherName = teacherName;
    }
    
    public static LeftClassDto from(ClassRoom classRoom) {
        return new LeftClassDto(
                classRoom.getClassId(),
                classRoom.getClassName(),
                classRoom.getDescription(),
                classRoom.getLocation().getName(),
                classRoom.getActivityDate(),
                classRoom.getTeacher().getName()
        );
    }
}