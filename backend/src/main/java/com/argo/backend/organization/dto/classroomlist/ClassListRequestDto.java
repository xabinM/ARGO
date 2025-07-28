package com.argo.backend.organization.dto.classroomlist;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassListRequestDto {
    private int pageNum;
    private int pageSize;
    private String status; // active, inactive, all
}
