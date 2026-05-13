package com.r786.studyflow.modules.auth.dto;

public record ResetPasswordRequest(String email, String otp, String newPassword) {}
