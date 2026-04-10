package com.r786.studyflow.modules.quiz.repository;

import com.r786.studyflow.modules.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // Fetch all quizzes belonging to a specific course
    List<Quiz> findByCourseId(Long courseId);

    // Optimized fetch for Admin Analytics: Get quiz with question count
    @Query("SELECT q, COUNT(qn) FROM Quiz q LEFT JOIN q.questions qn WHERE q.course.id = :courseId GROUP BY q")
    List<Object[]> findQuizzesWithQuestionCount(Long courseId);
}
