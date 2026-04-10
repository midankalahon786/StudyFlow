package com.r786.studyflow.modules.quiz.repository;

import com.r786.studyflow.modules.quiz.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    long countByQuizId(Long quizId);

    // Find all attempts by a specific student
    List<QuizSubmission> findByStudentId(Long studentId);

    // Find a student's attempt for a specific quiz
    Optional<QuizSubmission> findByQuizIdAndStudentId(Long quizId, Long studentId);

    // For Analytics: Calculate average score for a quiz
    @Query("SELECT AVG(s.score) FROM QuizSubmission s WHERE s.quiz.id = :quizId")
    Double findAverageScoreByQuizId(Long quizId);
}
