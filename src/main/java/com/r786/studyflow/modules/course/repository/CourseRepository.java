package com.r786.studyflow.modules.course.repository;

import com.r786.studyflow.modules.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByDepartment(String department);
    List<Course> findByTeacherId(Long teacherId);
}
