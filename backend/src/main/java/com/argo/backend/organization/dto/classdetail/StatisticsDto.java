package com.argo.backend.organization.dto.classdetail;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatisticsDto {
    private final Integer totalStudents;
    private final Integer totalTeams;

    public static StatisticsDto from(int studentCount, int teamCount) {
        return StatisticsDto.builder()
                .totalStudents(studentCount)
                .totalTeams(teamCount)
                .build();
    }
}