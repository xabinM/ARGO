package com.argo.backend.organization.dto.classdelete;

import com.argo.backend.domain.classroom.ClassApplication;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ApplicationDeletedData {
    private final int count;
    private final int pendingCount;
    private final int approvedCount;
    private final int rejectedCount;

    public static ApplicationDeletedData from(List<ClassApplication> applications) {
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

        return ApplicationDeletedData.builder()
                .count(applications.size())
                .pendingCount(pending)
                .approvedCount(approved)
                .rejectedCount(rejected)
                .build();
    }
}