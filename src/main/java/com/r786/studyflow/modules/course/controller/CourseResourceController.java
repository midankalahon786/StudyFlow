package com.r786.studyflow.modules.course.controller;

import com.r786.studyflow.modules.course.dto.CourseRequest;
import com.r786.studyflow.modules.course.entity.Course;
import com.r786.studyflow.modules.course.service.CourseResourceService;
import com.r786.studyflow.modules.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class CourseResourceController {
    private final CourseResourceService resourceService;
    private final CourseService courseService;

    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestParam("courseId") Long courseId,
            @RequestParam("teacherId") Long teacherId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file
    ){
        return ResponseEntity.ok(resourceService.uploadResource(courseId, teacherId, title, description, file));
    }

    @Operation(summary = "Update course details", description = "Updates metadata. Uses Optimistic Locking to handle concurrent edits.")
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Resource file = resourceService.loadResource(id);

        // This allows the browser to recognize the original file name
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
