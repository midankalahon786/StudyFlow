package com.r786.studyflow.modules.course.controller;

import com.r786.studyflow.modules.course.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping("/enroll")
    public ResponseEntity<?> enrollStudent(@RequestBody EnrollmentRequest request){
        enrollmentService.enrollStudent(request.courseId(), request.studentId());
        return ResponseEntity.ok(Map.of("message","Student enrolled successfully"));
    }

    public record EnrollmentRequest(Long courseId, Long studentId) {}
}

