package com.r786.studyflow.modules.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(description = "Detailed student performance metrics")
public record StudentPerformanceResponse(
        @Schema(description = "Overall average score across all quizzes") double overallAverage,
        @Schema(description = "Number of quizzes completed") int quizzesCompleted,
        @Schema(description = "Map of Quiz Titles to Scores") Map<String, Integer> quizResults,
        @Schema(description = "List of courses the student is currently enrolled in") List<String> enrolledCourses
) {}