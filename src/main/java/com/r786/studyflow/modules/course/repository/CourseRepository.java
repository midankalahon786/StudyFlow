package com.r786.studyflow.modules.course.repository;

import com.r786.studyflow.modules.course.entity.Course;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByDepartment(String department);

    List<Course> findByManagerId(Long managerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdWithLock(Long id);

    @Query("SELECT DISTINCT c FROM Course c " +
            "JOIN FETCH c.manager m " +
            "LEFT JOIN FETCH c.associatedTeachers " +
            "LEFT JOIN FETCH c.enrollments")
    List<Course> findAllWithAllParticipants();
}
