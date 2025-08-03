package com.argo.backend.organization.dto.applicationprocess;

import java.util.List;

public record ApplicationProcessResponse(
        List<ApplicationProcessResultDto> data
) {
    public static ApplicationProcessResponse from(List<ApplicationProcessResultDto> results) {
        return new ApplicationProcessResponse(results);
    }
}