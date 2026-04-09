package com.r786.studyflow.core.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleConflict(DataIntegrityViolationException ex){
        String message = ex.getMostSpecificCause().getMessage();

        if(message.contains("uq_user_email")){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error","This email is already registered."));
        }
        if(message.contains("uq_user_username")){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error","Username is already taken."));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error","Database error occurred."));

    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
