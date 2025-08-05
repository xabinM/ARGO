package com.argo.backend.organization.dto.classlist;

import lombok.Getter;

import java.util.List;

@Getter
public class ClassListResponse {
    
    private boolean success;
    private String message;
    private ClassListData data;
    
    public ClassListResponse(boolean success, String message, ClassListData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    @Getter
    public static class ClassListData {
        private List<ClassInfoDto> classes;
        private PaginationDto pagination;
        
        public ClassListData(List<ClassInfoDto> classes, PaginationDto pagination) {
            this.classes = classes;
            this.pagination = pagination;
        }
    }
    
    public static ClassListResponse success(List<ClassInfoDto> classes, PaginationDto pagination) {
        return new ClassListResponse(
                true,
                "반 목록 조회 성공",
                new ClassListData(classes, pagination)
        );
    }
    
    public static ClassListResponse error(String message) {
        return new ClassListResponse(false, message, null);
    }
}