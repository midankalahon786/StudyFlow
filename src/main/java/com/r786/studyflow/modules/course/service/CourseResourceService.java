package com.r786.studyflow.modules.course.service;

import com.r786.studyflow.core.service.FileStorageService;
import com.r786.studyflow.modules.course.entity.CourseResource;
import com.r786.studyflow.modules.course.repository.CourseRepository;
import com.r786.studyflow.modules.course.repository.CourseResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CourseResourceService {
    private final CourseResourceRepository resourceRepository;
    private final CourseRepository courseRepository;
    private final FileStorageService fileStorageService;

    public CourseResource uploadResource(Long courseId, String title, String description, MultipartFile file) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        String filePath = fileStorageService.storeFile(file, "courses/" + courseId);

        var resource = CourseResource.builder()
                .course(course)
                .teacher(course.getTeacher())
                .title(title)
                .description(description)
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileMimeType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        return resourceRepository.save(resource);

    }
}
