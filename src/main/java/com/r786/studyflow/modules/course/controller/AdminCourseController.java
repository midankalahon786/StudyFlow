package com.r786.studyflow.modules.course.controller;

import com.r786.studyflow.modules.course.repository.CourseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin Course Operations", description = "Endpoints for platform administrators to monitor courses")
@RestController
@RequestMapping("/api/v1/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCourseController {
    private final CourseRepository courseRepository;

    @Operation(summary = "Get system-wide course overview", description = "Fetches courses along with managers, staff counts, and total students.")
    @GetMapping("/overview")
    public ResponseEntity<List<AdminCourseResponse>> getSystemOverview() {
        var courses = courseRepository.findAllWithAllParticipants();

        var response = courses.stream().map(c -> new AdminCourseResponse(
                c.getTitle(),
                c.getManager().getUser().getUsername(),
                c.getAssociatedTeachers().size(),
                c.getNoOfStudentsEnrolled()
        )).toList();

        return ResponseEntity.ok(response);
    }

    public record AdminCourseResponse(
            String courseTitle,
            String managerName,
            int staffCount,
            int studentCount
    ) {}
}
