package com.argo.backend.organization.dto.studentlist;

import com.argo.backend.domain.classroom.entity.ClassRoom;
import lombok.Getter;

@Getter
public class StudentClassInfoDto {
    
    private Long classId;
    private String className;
    private int totalStudents;
    private Integer maxStudents;
    
    public StudentClassInfoDto(Long classId, String className, int totalStudents, Integer maxStudents) {
        this.classId = classId;
        this.className = className;
        this.totalStudents = totalStudents;
        this.maxStudents = maxStudents;
    }
    
    public static StudentClassInfoDto from(ClassRoom classRoom, int totalStudents) {
        return new StudentClassInfoDto(
                classRoom.getClassId(),
                classRoom.getClassName(),
                totalStudents,
                classRoom.getMaxStudents()
        );
    }
}