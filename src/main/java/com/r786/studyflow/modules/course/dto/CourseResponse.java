package com.r786.studyflow.modules.course.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CourseResponse(
        Long id,
        String title,
        String description,
        String department,
        String managerName,
        Integer enrollmentCount,
        List<String> associateTeacherNames,
        LocalDateTime createdAt
) {}
