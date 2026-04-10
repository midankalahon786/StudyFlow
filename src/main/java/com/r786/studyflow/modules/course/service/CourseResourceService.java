package com.r786.studyflow.modules.course.service;

import com.r786.studyflow.core.exceptions.GlobalExceptionHandler;
import com.r786.studyflow.core.service.FileStorageService;
import com.r786.studyflow.modules.auth.repository.TeacherRepository;
import com.r786.studyflow.modules.course.entity.CourseResource;
import com.r786.studyflow.modules.course.repository.CourseRepository;
import com.r786.studyflow.modules.course.repository.CourseResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class CourseResourceService {
    private final CourseResourceRepository resourceRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final FileStorageService fileStorageService;

    // Inside CourseResourceService.java
    public Resource loadResource(Long resourceId) {
        var metadata = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("File record not found"));

        try {
            Path filePath = fileStorageService.loadFile(metadata.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new GlobalExceptionHandler.ResourceNotFoundException("File not found on disk");
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File path is invalid", ex);
        }
    }

    public CourseResource uploadResource(Long courseId,Long uploaderTeacherId, String title, String description, MultipartFile file) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        var uploader = teacherRepository.findById(uploaderTeacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        boolean isAuthorized = course.getManager().getId().equals(uploaderTeacherId) ||
                course.getAssociatedTeachers().contains(uploader);

        if (!isAuthorized) {
            throw new RuntimeException("Access Denied: You are not authorized to add resources to this course");
        }

        String filePath = fileStorageService.storeFile(file, "courses/" + courseId);

        var resource = CourseResource.builder()
                .course(course)
                .teacher(uploader)
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
