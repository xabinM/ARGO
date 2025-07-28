package com.argo.backend.organization.service.exception;

public class ClassNotFoundException extends RuntimeException {
    public ClassNotFoundException(String message) {
        super(message);
    }
}