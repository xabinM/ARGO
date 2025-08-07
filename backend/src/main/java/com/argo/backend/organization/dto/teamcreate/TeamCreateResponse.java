package com.argo.backend.organization.dto.teamcreate;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record TeamCreateResponse(
        Long teamId,
        String teamName,
        Long classId,
        String className,
        Integer maxMembers,
        Integer currentMembers,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}