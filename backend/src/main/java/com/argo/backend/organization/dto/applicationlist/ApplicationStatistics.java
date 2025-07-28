package com.argo.backend.organization.dto.applicationlist;

import com.argo.backend.domain.classroom.ClassApplication;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ApplicationStatistics {
    private final Integer totalApplications;
    private final Integer pendingCount;
    private final Integer approvedCount;
    private final Integer rejectedCount;

    public static ApplicationStatistics from(List<ClassApplication> applications) {
        int pending = 0;
        int approved = 0;
        int rejected = 0;

        for (ClassApplication app : applications) {
            switch (app.getStatus()) {
                case PENDING -> pending++;
                case APPROVED -> approved++;
                case REJECTED -> rejected++;
            }
        }

        return ApplicationStatistics.builder()
                .totalApplications(applications.size())
                .pendingCount(pending)
                .approvedCount(approved)
                .rejectedCount(rejected)
                .build();
    }
}