package com.r786.studyflow.modules.auth.dto;

import com.r786.studyflow.modules.auth.entity.Role;
import lombok.Builder;

@Builder
public record UserResponse(
        Integer id,
        String username,
        String email,
        Role role
) {}