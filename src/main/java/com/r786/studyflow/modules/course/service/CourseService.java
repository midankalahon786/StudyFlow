package com.r786.studyflow.modules.course.service;

import com.r786.studyflow.modules.auth.entity.Teacher;
import com.r786.studyflow.modules.course.entity.Course;
import com.r786.studyflow.modules.course.repository.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    @Transactional
    public Course createCourse(String title, String description, Teacher manager) {
        return courseRepository.save(Course.builder()
                .title(title)
                .description(description)
                .manager(manager)
                .build());
    }

    public Course getCourseDetails(Long courseId, Long requesterTeacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if requester is manager or staff
        boolean isStaff = course.getManager().getId().equals(requesterTeacherId) ||
                course.getAssociatedTeachers().stream()
                        .anyMatch(t -> t.getId().equals(requesterTeacherId));

        if (!isStaff) {
            throw new RuntimeException("Access Denied: You do not have permission to view this course's content");
        }

        return course;
    }

    @Transactional
    public void addAssociatedTeacher(Long courseId, Teacher associate, Long managerId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Security check: Only the manager can add co-teachers
        if (!course.getManager().getId().equals(managerId)) {
            throw new RuntimeException("Access Denied: Only the manager can add staff");
        }

        course.getAssociatedTeachers().add(associate);
    }
}
