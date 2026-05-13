package com.r786.studyflow.modules.auth.dto;

import com.r786.studyflow.modules.auth.entity.Role;

public record RegisterRequest(
        String username,
        String password,
        String firstName,
        String lastName,
        String email,
        Role role,

        // Student specific fields
        String enrollmentNo,
        Integer semester,
        String batchYear,

        // Teacher specific fields
        String department,
        String empId,
        String designation
) {}



