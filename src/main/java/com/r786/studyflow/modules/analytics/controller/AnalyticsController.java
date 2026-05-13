package com.r786.studyflow.modules.analytics.controller;

import com.r786.studyflow.modules.analytics.dto.StudentPerformanceResponse;
import com.r786.studyflow.modules.analytics.dto.TeacherAnalyticsResponse;
import com.r786.studyflow.modules.analytics.service.AnalyticsService;
import com.r786.studyflow.modules.auth.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get teacher dashboard data", description = "Returns averages and top performers")
    public ResponseEntity<TeacherAnalyticsResponse> getCourseAnalytics(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User user) {

        // Extract the ID from the authenticated user.
        // Due to @MapsId, this is the same as the Teacher ID.
        return ResponseEntity.ok(analyticsService.getCourseAnalytics(courseId, user.getId()));
    }
}
