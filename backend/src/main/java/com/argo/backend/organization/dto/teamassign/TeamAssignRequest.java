package com.argo.backend.organization.dto.teamassign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamAssignRequest {
    private List<Long> studentIds;

    public boolean isValid() {
        return studentIds != null && !studentIds.isEmpty();
    }

    public int getStudentCount() {
        return studentIds != null ? studentIds.size() : 0;
    }
}