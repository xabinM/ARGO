package com.argo.backend.organization.dto.teamassign;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class TeamAssignRequest {
    
    @NotEmpty(message = "학생 ID 목록은 필수입니다.")
    private List<Long> studentIds;
}