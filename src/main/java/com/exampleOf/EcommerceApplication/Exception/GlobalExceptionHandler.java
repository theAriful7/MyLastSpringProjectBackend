package com.exampleOf.EcommerceApplication.Exception;

import com.exampleOf.EcommerceApplication.Exception.CustomException.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status, String path) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                "💡 Tip: Breathe... debug... and try again!"
        );
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, req.getRequestURI());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, req.getRequestURI());
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidation(BusinessValidationException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, req.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedAccessException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, req.getRequestURI());
    }

    @ExceptionHandler(OperationFailedException.class)
    public ResponseEntity<ErrorResponse> handleOperation(OperationFailedException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, req.getRequestURI());
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponse> handleDatabase(DatabaseException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, req.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, req.getRequestURI());
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenActionException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN, req.getRequestURI());
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(AlreadyExistsException ex, HttpServletRequest req) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
        return buildResponse("💣 Unexpected crash: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, req.getRequestURI());
    }
}
