package com.argo.backend.organization.dto.classdelete;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassDeleteResponse {
    private final ClassDeletedInfo deletedClass;
    private final DeletedData deletedData;

    public static ClassDeleteResponse of(
            ClassDeletedInfo deletedClass,
            DeletedData deletedData
    ) {
        return ClassDeleteResponse.builder()
                .deletedClass(deletedClass)
                .deletedData(deletedData)
                .build();
    }
}