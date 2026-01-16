package com.stockviewer.stockapi.exception;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(404)
                .body(new ErrorResponse(
                        "404",
                        List.of(new FieldErrorDTO("Bad credentials", ex.getMessage()))));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldErrorDTO> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.add(new FieldErrorDTO(error.getField(), error.getDefaultMessage()))
        );
        return ResponseEntity
                .status(400)
                .body(new ErrorResponse(
                        "400",
                        fieldErrors));
    }

    @ExceptionHandler(TimeFrameNotSupported.class)
    public ResponseEntity<ErrorResponse> handleTimeFrameNotSupportedExceptions(TimeFrameNotSupported ex) {
        return ResponseEntity
                .status(400)
                .body(new ErrorResponse(
                        "400",
                        List.of(new FieldErrorDTO("Not supported timeframe", ex.getMessage()))));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsExceptions(BadCredentialsException ex) {
        return ResponseEntity
                .status(401)
                .body(new ErrorResponse(
                        "401",
                        List.of(new FieldErrorDTO("Bad credentials", ex.getMessage()))));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateException(DuplicateException ex) {
        return ResponseEntity
                .status(409)
                .body(new ErrorResponse(
                        "409",
                        List.of(new FieldErrorDTO("Duplicate record", ex.getMessage()))
                ));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(InsufficientFundsException ex) {
        return ResponseEntity
                .status(400)
                .body(new ErrorResponse(
                        "400",
                        List.of(new FieldErrorDTO("Insufficient funds", ex.getMessage()))));
    }

    @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
                return ResponseEntity
                        .status(403)
                        .body(new ErrorResponse(
                                "403",
                                List.of(new FieldErrorDTO("Forbidden", ex.getMessage()))));
        }
}