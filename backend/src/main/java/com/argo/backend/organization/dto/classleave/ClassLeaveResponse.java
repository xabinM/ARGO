package com.argo.backend.organization.dto.classleave;

import lombok.Getter;

@Getter
public class ClassLeaveResponse {
    
    private LeftClassDto leftClass;
    private StudentInfoDto studentInfo;
    private TeamInfoDto teamInfo;
    private ClassStatusDto classStatus;
    
    public ClassLeaveResponse(LeftClassDto leftClass, StudentInfoDto studentInfo, 
                             TeamInfoDto teamInfo, ClassStatusDto classStatus) {
        this.leftClass = leftClass;
        this.studentInfo = studentInfo;
        this.teamInfo = teamInfo;
        this.classStatus = classStatus;
    }
    
    public static ClassLeaveResponse of(LeftClassDto leftClass, StudentInfoDto studentInfo,
                                       TeamInfoDto teamInfo, ClassStatusDto classStatus) {
        return new ClassLeaveResponse(leftClass, studentInfo, teamInfo, classStatus);
    }
}