package com.argo.backend.organization.service.exception;

public class TeacherNotAllowedException extends RuntimeException {
    public TeacherNotAllowedException(String message) {
        super(message);
    }
}