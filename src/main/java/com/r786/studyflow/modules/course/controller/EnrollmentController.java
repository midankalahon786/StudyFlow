package com.r786.studyflow.modules.course.controller;

import com.r786.studyflow.modules.course.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Enrollment Management", description = "Endpoints for students to enroll in courses")
@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @Operation(summary = "Enroll a student in a course", description = "Requires a valid courseId and studentId. Uses Pessimistic Locking to ensure capacity limits.")
    @PostMapping("/enroll")
    public ResponseEntity<?> enrollStudent(@RequestBody EnrollmentRequest request){
        enrollmentService.enrollStudent(request.courseId(), request.studentId());
        return ResponseEntity.ok(Map.of("message","Student enrolled successfully"));
    }

    public record EnrollmentRequest(Long courseId, Long studentId) {}
}

