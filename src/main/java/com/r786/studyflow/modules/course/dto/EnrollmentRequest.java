package com.r786.studyflow.modules.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EnrollmentRequest(
        @Schema(description = "Unique ID of the course", example = "1") Long courseId,
        @Schema(description = "Unique ID of the student", example = "42") Long studentId
) {}
