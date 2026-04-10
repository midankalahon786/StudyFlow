package com.r786.studyflow.modules.course.service;

import com.r786.studyflow.modules.auth.repository.StudentRepository;
import com.r786.studyflow.modules.course.entity.CourseStudent;
import com.r786.studyflow.modules.course.repository.CourseRepository;
import com.r786.studyflow.modules.course.repository.CourseStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final CourseStudentRepository enrollmentRepository;

    @Transactional
    public void enrollStudent(Long courseId, Long studentId){
        var course = courseRepository.findByIdWithLock(courseId)
                .orElseThrow(()->new RuntimeException("Course not found"));

        var student = studentRepository.findById(studentId)
                .orElseThrow(()-> new RuntimeException("Student not found"));

        if(enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)){
            throw new IllegalStateException("Student is already enrolled");
        }

        enrollmentRepository.save(CourseStudent.builder()
                .course(course)
                .student(student)
                .build());

        // Safe increment due to PESSIMISTIC_WRITE lock
        course.setNoOfStudentsEnrolled(course.getNoOfStudentsEnrolled() + 1);
        courseRepository.save(course);
    }
}
