package com.r786.studyflow.modules.course.service;

import com.r786.studyflow.core.exceptions.GlobalExceptionHandler;
import com.r786.studyflow.core.service.FileStorageService;
import com.r786.studyflow.modules.auth.repository.TeacherRepository;
import com.r786.studyflow.modules.course.entity.CourseResource;
import com.r786.studyflow.modules.course.repository.CourseRepository;
import com.r786.studyflow.modules.course.repository.CourseResourceRepository;
import com.r786.studyflow.modules.course.repository.CourseStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class CourseResourceService {
    private final CourseResourceRepository resourceRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final CourseStudentRepository enrollmentRepository; // Added this
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public Resource loadResource(Long resourceId, Long requesterId, String role) {
        var metadata = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("File record not found"));

        Long courseId = metadata.getCourse().getId();

        // Security Check
        if ("ROLE_STUDENT".equals(role)) {
            if (!enrollmentRepository.existsByCourseIdAndStudentId(courseId, requesterId)) {
                throw new AccessDeniedException("You must be enrolled to access this resource");
            }
        } else if ("ROLE_TEACHER".equals(role)) {
            var course = metadata.getCourse();
            boolean isStaff = course.getManager().getId().equals(requesterId) ||
                    course.getAssociatedTeachers().stream().anyMatch(t -> t.getId().equals(requesterId));
            if (!isStaff) throw new AccessDeniedException("You are not staff for this course");
        }

        try {
            Path filePath = fileStorageService.loadFile(metadata.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) return resource;
            else throw new GlobalExceptionHandler.ResourceNotFoundException("File not found on disk");
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Invalid file path", ex);
        }
    }

    @Transactional
    public CourseResource uploadResource(Long courseId, Long uploaderId, String title, String description, MultipartFile file) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Course not found"));

        // Use ID check to avoid LazyInitialization issues with .contains()
        boolean isAuthorized = course.getManager().getId().equals(uploaderId) ||
                course.getAssociatedTeachers().stream().anyMatch(t -> t.getId().equals(uploaderId));

        if (!isAuthorized) {
            throw new AccessDeniedException("Not authorized to upload to this course");
        }

        // Store file and get path
        String filePath = fileStorageService.storeFile(file, "courses/" + courseId);

        return resourceRepository.save(CourseResource.builder()
                .course(course)
                .teacher(teacherRepository.getReferenceById(uploaderId)) // Optimization: use reference
                .title(title)
                .description(description)
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileMimeType(file.getContentType())
                .fileSize(file.getSize())
                .build());
    }
}
