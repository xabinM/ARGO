package com.argo.backend.organization.dto.teamdelete;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeletedTeamInfo {
    private final Long teamId;
    private final String teamName;
    private final Long classId;
    private final String className;
    private final LocalDateTime deletedAt;
}