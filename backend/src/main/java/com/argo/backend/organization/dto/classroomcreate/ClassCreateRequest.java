package com.argo.backend.organization.dto.classroomcreate;

/**
 * 반 생성 요청 데이터를 담는 DTO 클래스
 * 클라이언트에서 반 생성 시 필요한 정보들을 전달받기 위한 요청 객체
 */

import lombok.*;

import java.time.LocalDate;

@Getter
public class ClassCreateRequest {

    private String className;
    
    private String description;

    private LocalDate activityDate;

    private Integer maxStudents;

    private String location;

    private Integer grade;

}
