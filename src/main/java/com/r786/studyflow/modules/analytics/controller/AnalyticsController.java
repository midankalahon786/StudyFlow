package com.r786.studyflow.modules.analytics.controller;

import com.r786.studyflow.modules.analytics.dto.StudentPerformanceResponse;
import com.r786.studyflow.modules.analytics.dto.TeacherAnalyticsResponse;
import com.r786.studyflow.modules.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics Module", description = "Endpoints for student and course performance metrics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get performance report for a student",
            description = "Aggregates quiz scores and enrollment data into a single report.")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentPerformanceResponse> getStudentReport(@PathVariable Long studentId) {
        return ResponseEntity.ok(analyticsService.getStudentPerformance(studentId));
    }

    @Operation(summary = "Get course performance for teachers",
            description = "Returns class averages and enrollment metrics. Requires teacher authorization.")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<TeacherAnalyticsResponse> getCourseReport(
            @PathVariable Long courseId,
            @RequestParam Long teacherId) {
        return ResponseEntity.ok(analyticsService.getCourseAnalytics(courseId, teacherId));
    }
}
