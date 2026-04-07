package com.r786.studyflow.modules.auth.dto;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken
) {
}
