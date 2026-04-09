package com.r786.studyflow.modules.auth.repository;

import com.r786.studyflow.modules.auth.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByEnrollmentNo(String enrollmentNo);
}
