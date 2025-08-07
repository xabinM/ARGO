package com.argo.backend.organization.dto.applicationprocess;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.util.List;

@Getter
public class ApplicationProcessRequest {
    
    @NotBlank(message = "처리 유형은 필수입니다.")
    @Pattern(regexp = "^(approve|reject)$", message = "처리 유형은 approve 또는 reject만 가능합니다.")
    @JsonProperty("action")
    private String action;
    
    @NotEmpty(message = "신청 ID 목록은 필수입니다.")
    @JsonProperty("applicationIds")
    private List<Long> applicationIds;
}