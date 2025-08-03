package com.argo.backend.organization.dto.classleave;

import lombok.Getter;

@Getter
public class ClassStatusDto {
    
    private int totalStudents;
    private int maxStudents;
    private int availableSlots;
    
    public ClassStatusDto(int totalStudents, int maxStudents, int availableSlots) {
        this.totalStudents = totalStudents;
        this.maxStudents = maxStudents;
        this.availableSlots = availableSlots;
    }
    
    public static ClassStatusDto of(int totalStudents, int maxStudents) {
        return new ClassStatusDto(totalStudents, maxStudents, maxStudents - totalStudents);
    }
}