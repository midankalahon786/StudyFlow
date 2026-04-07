package com.r786.studyflow.modules.auth.dto;

import com.r786.studyflow.modules.auth.entity.Role;

public record RegisterRequest(
        String username,
        String password,
        String email,
        String firstname,
        String lastname,
        Role role
){}



