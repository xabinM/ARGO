package com.argo.backend.organization.dto.classdelete;

import lombok.Getter;

@Getter
public class ClassDeleteResponse {
    
    private DeletedClassDto deletedClass;
    private DeletedDataDto deletedData;
    
    public ClassDeleteResponse(DeletedClassDto deletedClass, DeletedDataDto deletedData) {
        this.deletedClass = deletedClass;
        this.deletedData = deletedData;
    }
    
    public static ClassDeleteResponse of(DeletedClassDto deletedClass, DeletedDataDto deletedData) {
        return new ClassDeleteResponse(deletedClass, deletedData);
    }
}