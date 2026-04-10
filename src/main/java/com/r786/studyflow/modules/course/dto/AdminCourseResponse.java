package com.r786.studyflow.modules.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminCourseResponse(
        @Schema(description = "The title of the course") String courseTitle,
        @Schema(description = "The username of the Central Manager") String managerName,
        @Schema(description = "Number of associated co-teachers") int staffCount,
        @Schema(description = "Total number of enrolled students") int studentCount
) {}
