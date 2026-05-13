package com.r786.studyflow.modules.auth.dto;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String email,
        // Teacher specific
        String department,
        // Student specific
        String enrollmentNo,
        Integer semester
) {}