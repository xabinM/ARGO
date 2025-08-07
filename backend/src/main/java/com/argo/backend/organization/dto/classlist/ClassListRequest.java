package com.argo.backend.organization.dto.classlist;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassListRequest {
    
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    private int page = 1;
    
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    private int size = 10;
    
    @Pattern(regexp = "^(active|inactive|all)$", message = "상태는 active, inactive, all 중 하나여야 합니다")
    private String status = "active";
}