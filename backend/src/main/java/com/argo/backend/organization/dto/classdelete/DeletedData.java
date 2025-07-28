package com.argo.backend.organization.dto.classdelete;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeletedData {
    private final StudentDeletedData students;
    private final TeamDeletedData teams;
    private final ApplicationDeletedData applications;

    public static DeletedData of(
            StudentDeletedData students,
            TeamDeletedData teams,
            ApplicationDeletedData applications
    ) {
        return DeletedData.builder()
                .students(students)
                .teams(teams)
                .applications(applications)
                .build();
    }
}