package com.argo.backend.organization.dto;

/**
 * 반 목록 조회 시 상태 필터링을 위한 Enum
 * 프론트엔드에서 전달받는 status 파라미터의 유효성을 검증하고 변환
 */
public enum ClassStatusFilter {
    ACTIVE("active"),
    INACTIVE("inactive"), 
    ALL("all");
    
    private final String value;
    
    ClassStatusFilter(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * 문자열 값을 ClassStatusFilter Enum으로 변환
     * @param status 문자열 상태값 (active, inactive, all)
     * @return 해당하는 ClassStatusFilter, 잘못된 값이면 ACTIVE 반환
     */
    public static ClassStatusFilter fromString(String status) {
        if (status == null) {
            return ACTIVE;
        }
        
        for (ClassStatusFilter filter : values()) {
            if (filter.value.equalsIgnoreCase(status)) {
                return filter;
            }
        }
        return ACTIVE; // 기본값
    }
    
    /**
     * 유효한 status 값인지 검증
     * @param status 검증할 문자열
     * @return 유효하면 true, 아니면 false
     */
    public static boolean isValid(String status) {
        if (status == null) {
            return true; // null은 기본값으로 처리
        }
        
        for (ClassStatusFilter filter : values()) {
            if (filter.value.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 사용 가능한 모든 값들을 문자열 배열로 반환
     * @return ["active", "inactive", "all"]
     */
    public static String[] getValidValues() {
        String[] values = new String[ClassStatusFilter.values().length];
        for (int i = 0; i < ClassStatusFilter.values().length; i++) {
            values[i] = ClassStatusFilter.values()[i].getValue();
        }
        return values;
    }
}