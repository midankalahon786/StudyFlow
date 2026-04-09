package com.r786.studyflow.modules.course.repository;

import com.r786.studyflow.modules.course.entity.CourseStudent;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CourseStudentRepository extends JpaRepository<CourseStudent, Long> {
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}
