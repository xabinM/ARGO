package com.argo.backend.organization.exception;

import com.argo.backend.global.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.argo.backend.organization")
public class OrganizationExceptionHandler {

    @ExceptionHandler({
        ClassNotFoundException.class,
        UnauthorizedClassAccessException.class,
        UserNotFoundException.class,
        LocationNotFoundException.class,
        ApplicationNotFoundException.class,
        StudentNotFoundException.class,
        TeamNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundException(RuntimeException e) {
        ErrorResponse error = new ErrorResponse("NOT_FOUND", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
        InsufficientPermissionException.class,
        StudentOnlyException.class
    })
    public ResponseEntity<ErrorResponse> handleForbiddenException(RuntimeException e) {
        ErrorResponse error = new ErrorResponse("FORBIDDEN", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
        DuplicateApplicationException.class,
        ClassNotAvailableException.class,
        ApplicationAlreadyProcessedException.class,
        DuplicateTeamNameException.class,
        StudentAlreadyAssignedException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictException(RuntimeException e) {
        ErrorResponse error = new ErrorResponse("CONFLICT", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
        InvalidInviteCodeException.class,
        ClassCapacityExceededException.class,
        TeamCapacityExceededException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestException(RuntimeException e) {
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
        InviteCodeGenerationException.class,
        TeamCreationFailedException.class
    })
    public ResponseEntity<ErrorResponse> handleInternalServerError(RuntimeException e) {
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}