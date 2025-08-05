package com.argo.backend.organization.dto.applicationlist;

import lombok.Getter;

@Getter
public class ApplicationListRequest {
    private String status;
    private int page;
    private int size;
}
