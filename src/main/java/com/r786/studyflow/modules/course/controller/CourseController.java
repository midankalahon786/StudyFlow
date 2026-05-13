package com.r786.studyflow.modules.course.controller;

import com.r786.studyflow.modules.auth.entity.User;
import com.r786.studyflow.modules.course.dto.CourseRequest;
import com.r786.studyflow.modules.course.dto.CourseResponse;
import com.r786.studyflow.modules.course.entity.Course;
import com.r786.studyflow.modules.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Course Module", description = "Endpoints for course lifecycle, management, and staff assignment")
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "Create a new course", description = "A teacher creates a course and becomes the central manager.")
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(
            @AuthenticationPrincipal User user, // Extracts ID from JWT
            @RequestBody CourseRequest request) {
        Course course = courseService.createCourse(request, user.getId());
        return ResponseEntity.ok(mapToResponse(course));
    }

    @Operation(summary = "Get detailed course view", description = "Retrieves course metadata and participants if the requester is staff.")
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        Course course = courseService.getCourseDetails(id, user.getId());
        return ResponseEntity.ok(mapToResponse(course));
    }

    @Operation(summary = "Update course metadata", description = "Updates title, description, and department using Optimistic Locking.")
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseRequest request) {
        Course updated = courseService.updateCourse(id, request);
        return ResponseEntity.ok(mapToResponse(updated)); // Consistency!
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

    private CourseResponse mapToResponse(Course course) {
        User managerUser = course.getManager().getUser();

        // Fallback logic: Use "First Last", or just "First", or fallback to username
        String fullName = (managerUser.getFirstName() != null ? managerUser.getFirstName() : "") +
                " " +
                (managerUser.getLastName() != null ? managerUser.getLastName() : "");

        if (fullName.trim().isEmpty()) {
            fullName = managerUser.getUsername(); // Fallback to username if names are null
        }

        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getDepartment(),
                fullName, // Cleaned up name
                course.getNoOfStudentsEnrolled() != null ? course.getNoOfStudentsEnrolled() : 0,
                course.getAssociatedTeachers() == null ? List.of() : course.getAssociatedTeachers().stream()
                                                                     .map(t -> {
                                                                         String name = t.getUser().getFirstName();
                                                                         return name != null ? name : t.getUser().getUsername();
                                                                     })
                                                                     .toList(),
                course.getCreatedAt()
        );
    }
}

