package com.argo.backend.organization.dto.randomassign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RandomAssignRequest {
    private String assignmentType;

    public String getAssignmentTypeWithDefault() {
        return assignmentType != null ? assignmentType : "balanced";
    }

    public boolean isValidAssignmentType() {
        String type = getAssignmentTypeWithDefault();
        return "balanced".equals(type) || "random".equals(type);
    }

    public boolean isBalancedType() {
        return "balanced".equals(getAssignmentTypeWithDefault());
    }
}