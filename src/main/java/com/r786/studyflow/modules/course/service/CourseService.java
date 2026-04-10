package com.r786.studyflow.modules.course.service;

import com.r786.studyflow.core.exceptions.GlobalExceptionHandler;
import com.r786.studyflow.modules.auth.repository.TeacherRepository;
import com.r786.studyflow.modules.course.dto.CourseRequest;
import com.r786.studyflow.modules.course.entity.Course;
import com.r786.studyflow.modules.course.repository.CourseRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public Course createCourse(CourseRequest request, Long managerId) {
        var manager = teacherRepository.findById(managerId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Teacher not found"));

        return courseRepository.save(Course.builder()
                .title(request.title()) // Record syntax
                .description(request.description())
                .department(request.department())
                .manager(manager)
                .build());
    }

    @Transactional(readOnly = true)
    public Course getCourseDetails(Long courseId, Long requesterId) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Course not found"));

        boolean isStaff = course.getManager().getId().equals(requesterId) ||
                course.getAssociatedTeachers().stream()
                        .anyMatch(t -> t.getId().equals(requesterId));

        if (!isStaff) {
            throw new IllegalStateException("Access Denied: You are not authorized staff");
        }
        return course;
    }

    @Transactional
    public void addAssociatedTeacher(Long courseId, Long associateId, Long managerId) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Course not found"));

        if (!course.getManager().getId().equals(managerId)) {
            throw new IllegalStateException("Access Denied: Only the manager can add staff");
        }

        var associate = teacherRepository.findById(associateId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Associate teacher not found"));

        course.getAssociatedTeachers().add(associate);
    }

    @Transactional
    public Course updateCourse(Long courseId, CourseRequest request) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Course not found"));

        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setDepartment(request.department());

        return courseRepository.save(course);
    }
}
