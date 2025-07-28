package com.argo.backend.organization.dto.classdelete;

import com.argo.backend.organization.dto.classdetail.StudentDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StudentDeletedData {
    private final int count;
    private final List<StudentDto> details;

    public static StudentDeletedData of(List<StudentDto> students) {
        return StudentDeletedData.builder()
                .count(students.size())
                .details(students)
                .build();
    }
}