package com.argo.backend.organization.exception;

import com.argo.backend.organization.dto.CommonApiResponse;
import com.argo.backend.organization.exception.types.*;
import com.argo.backend.organization.exception.types.ClassNotFoundException;
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
    public ResponseEntity<CommonApiResponse<Void>> handleNotFoundException(RuntimeException e) {
        CommonApiResponse<Void> response = new CommonApiResponse<>(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
        InsufficientPermissionException.class,
        StudentOnlyException.class
    })
    public ResponseEntity<CommonApiResponse<Void>> handleForbiddenException(RuntimeException e) {
        CommonApiResponse<Void> response = new CommonApiResponse<>(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
        DuplicateApplicationException.class,
        ClassNotAvailableException.class,
        ApplicationAlreadyProcessedException.class,
        DuplicateTeamNameException.class,
        StudentAlreadyAssignedException.class,
        ActivityInProgressException.class,
        CannotDeleteActiveClassException.class,
        NotParticipatingClassException.class
    })
    public ResponseEntity<CommonApiResponse<Void>> handleConflictException(RuntimeException e) {
        CommonApiResponse<Void> response = new CommonApiResponse<>(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
        InvalidInviteCodeException.class,
        ClassCapacityExceededException.class,
        TeamCapacityExceededException.class,
        TeamFullException.class,
        InvalidPageParameterException.class,
        InvalidClassIdException.class,
        InvalidStatusParameterException.class
    })
    public ResponseEntity<CommonApiResponse<Void>> handleBadRequestException(RuntimeException e) {
        CommonApiResponse<Void> response = new CommonApiResponse<>(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
        InviteCodeGenerationException.class,
        TeamCreationFailedException.class
    })
    public ResponseEntity<CommonApiResponse<Void>> handleInternalServerError(RuntimeException e) {
        CommonApiResponse<Void> response = new CommonApiResponse<>(false, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}