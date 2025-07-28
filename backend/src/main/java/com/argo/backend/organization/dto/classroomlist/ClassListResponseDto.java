package com.argo.backend.organization.dto.classroomlist;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ClassListResponseDto {
    private Long classId;
    private String className;
    private String description;
    private String location;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate activityDate;
    
    private Integer studentCount;
    private Integer maxStudents;
    private Integer teamCount;
    private String status;
    private String inviteCode;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}
