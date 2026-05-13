package com.r786.studyflow.modules.course.controller;

import com.r786.studyflow.modules.auth.entity.User;
import com.r786.studyflow.modules.course.dto.CourseRequest;
import com.r786.studyflow.modules.course.entity.Course;
import com.r786.studyflow.modules.course.entity.CourseResource;
import com.r786.studyflow.modules.course.service.CourseResourceService;
import com.r786.studyflow.modules.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class CourseResourceController {
    private final CourseResourceService resourceService;
    private final CourseService courseService;

    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CourseResource> uploadFile(
            @RequestParam("courseId") Long courseId,
            @AuthenticationPrincipal User user, // Derive teacherId from token
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file
    ){
        return ResponseEntity.ok(resourceService.uploadResource(courseId, user.getId(), title, description, file));
    }

    @Operation(summary = "Update course details", description = "Updates metadata. Uses Optimistic Locking to handle concurrent edits.")
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, @AuthenticationPrincipal User user) {
        // Pass user to loadResource for the security checks we built earlier
        Resource file = resourceService.loadResource(id, user.getId(), String.valueOf(user.getRole()));

        // Attempt to determine content type (MIME)
        String contentType = "application/octet-stream";
        // You could fetch this from your CourseResource metadata in the DB!

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
