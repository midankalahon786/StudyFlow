package com.r786.studyflow.modules.quiz.repository;

import com.r786.studyflow.modules.analytics.dto.TopPerformerDTO;
import com.r786.studyflow.modules.quiz.entity.QuizSubmission;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    boolean existsByQuizIdAndStudentId(Long quizId, Long studentId);
    long countByQuizId(Long quizId);

    // Find all attempts by a specific student
    List<QuizSubmission> findByStudentId(Long studentId);

    // Find a student's attempt for a specific quiz
    Optional<QuizSubmission> findByQuizIdAndStudentId(Long quizId, Long studentId);

    // For Analytics: Calculate average score for a quiz
    @Query("SELECT AVG(s.score) FROM QuizSubmission s WHERE s.quiz.id = :quizId")
    Double findAverageScoreByQuizId(Long quizId);

    @Query("SELECT new com.r786.studyflow.modules.analytics.dto.TopPerformerDTO(" +
            "CONCAT(s.student.user.firstName, ' ', s.student.user.lastName), " +
            "SUM(s.score)) " + // SUM returns Long
            "FROM QuizSubmission s " +
            "WHERE s.quiz.course.id = :courseId " +
            "GROUP BY s.student.id, s.student.user.firstName, s.student.user.lastName " +
            "ORDER BY SUM(s.score) DESC")
    List<TopPerformerDTO> findTopPerformersByCourseId(@Param("courseId") Long courseId, PageRequest pageable);

}
