package com.r786.studyflow.core.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiError {
    private int status;        // e.g., 404
    private String message;    // e.g., "Choice not found"
    private LocalDateTime timestamp;
}