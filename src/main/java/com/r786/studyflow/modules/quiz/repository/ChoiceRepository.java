package com.r786.studyflow.modules.quiz.repository;

import com.r786.studyflow.modules.quiz.entity.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChoiceRepository extends JpaRepository<Choice, Long> {
    List<Choice> findByQuestionId(Long questionId);

    // Used for automated grading: Find all correct choices for a list of question IDs
    List<Choice> findByQuestionIdInAndIsCorrectTrue(List<Long> questionIds);
}
