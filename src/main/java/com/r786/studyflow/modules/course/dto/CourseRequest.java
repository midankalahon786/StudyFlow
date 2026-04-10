package com.r786.studyflow.modules.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request object for creating or updating a course")
public record CourseRequest(
        @NotBlank(message = "Title is required")
        @Schema(description = "The title of the course", example = "Advanced Java Programming")
        String title,

        @Schema(description = "Detailed description of the course content", example = "Deep dive into Spring Boot and Microservices.")
        String description,

        @NotBlank(message = "Department is required")
        @Schema(description = "The academic department offering the course", example = "Computer Science")
        String department
) {}