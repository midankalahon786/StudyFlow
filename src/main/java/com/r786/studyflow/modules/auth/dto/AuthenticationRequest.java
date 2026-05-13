package com.r786.studyflow.modules.auth.dto;

public record AuthenticationRequest(
        String username,
        String password,
        String email
) { }
