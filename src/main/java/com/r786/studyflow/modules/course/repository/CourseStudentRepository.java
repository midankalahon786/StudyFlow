package com.r786.studyflow.modules.course.repository;

import com.r786.studyflow.modules.course.entity.CourseStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CourseStudentRepository extends JpaRepository<CourseStudent, Long> {
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
    List<CourseStudent> findAllByStudentId(Long studentId);
    List<CourseStudent> findAllByCourseId(Long courseId);
}
