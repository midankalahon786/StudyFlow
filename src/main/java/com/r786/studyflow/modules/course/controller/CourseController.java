package com.r786.studyflow.modules.course.controller;

import com.r786.studyflow.modules.course.dto.CourseRequest;
import com.r786.studyflow.modules.course.entity.Course;
import com.r786.studyflow.modules.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Course Module", description = "Endpoints for course lifecycle, management, and staff assignment")
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "Create a new course", description = "A teacher creates a course and becomes the central manager.")
    @PostMapping
    public ResponseEntity<Course> createCourse(
            @RequestParam Long managerId,
            @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.createCourse(request, managerId));
    }

    @Operation(summary = "Get detailed course view", description = "Retrieves course metadata and participants if the requester is staff.")
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseDetails(
            @PathVariable Long id,
            @RequestParam Long requesterId) {
        return ResponseEntity.ok(courseService.getCourseDetails(id, requesterId));
    }

    @Operation(summary = "Update course metadata", description = "Updates title, description, and department using Optimistic Locking.")
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @Operation(summary = "Add associated teacher", description = "Only the Central Manager can add co-teachers to the course.")
    @PostMapping("/{id}/teachers")
    public ResponseEntity<Void> addStaff(
            @PathVariable Long id,
            @RequestParam Long associateId,
            @RequestParam Long managerId) {
        courseService.addAssociatedTeacher(id, associateId, managerId);
        return ResponseEntity.noContent().build();
    }
}
