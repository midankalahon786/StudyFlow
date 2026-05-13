package com.r786.studyflow.modules.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Schema(description = "Aggregated performance metrics for a specific course")
public record TeacherAnalyticsResponse(
        @Schema(description = "Total number of students enrolled")
        int totalStudents,

        @Schema(description = "Map of Quiz Titles to Class Average Scores")
        Map<String, Double> classAverages,

        @Schema(description = "List of top performing students with their total scores")
        List<TopPerformerDTO> topPerformers, // FIX: Use the DTO here

        @Schema(description = "Average completion rate of quizzes in the course")
        double completionRate
) {}


