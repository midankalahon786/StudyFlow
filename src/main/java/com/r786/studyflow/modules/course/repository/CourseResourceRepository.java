package com.r786.studyflow.modules.course.repository;

import com.r786.studyflow.modules.course.entity.CourseResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseResourceRepository extends JpaRepository<CourseResource, Long> {
    List<CourseResource> findByCourseId(Long courseId);
}
