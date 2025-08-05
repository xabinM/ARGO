package com.argo.backend.organization.dto.teamcreate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class TeamCreateRequest {
    
    @NotBlank(message = "팀 이름을 입력해주세요.")
    private String teamName;
    
    @Positive(message = "최대 인원은 양수여야 합니다.")
    private Integer maxMembers;
}