package com.r786.studyflow.modules.quiz.dto;

import java.util.List;

public record QuizRequest(
        Long courseId,
        String title,
        String description,
        Integer timeLimitInMinutes,
        List<QuestionRequest> questions
) {}
