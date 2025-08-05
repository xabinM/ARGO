package com.argo.backend.organization.dto.classdelete;

import com.argo.backend.domain.user.User;
import lombok.Getter;

import java.util.List;

@Getter
public class DeletedStudentsDto {
    
    private int count;
    private List<StudentDetailDto> details;
    
    public DeletedStudentsDto(int count, List<StudentDetailDto> details) {
        this.count = count;
        this.details = details;
    }
    
    public static DeletedStudentsDto of(List<User> students) {
        List<StudentDetailDto> details = students.stream()
                .map(StudentDetailDto::from)
                .toList();
        return new DeletedStudentsDto(students.size(), details);
    }
}

@Getter
class StudentDetailDto {
    
    private Long studentId;
    private String studentName;
    
    public StudentDetailDto(Long studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
    }
    
    public static StudentDetailDto from(User student) {
        return new StudentDetailDto(
                student.getUserId(),
                student.getName()
        );
    }
}