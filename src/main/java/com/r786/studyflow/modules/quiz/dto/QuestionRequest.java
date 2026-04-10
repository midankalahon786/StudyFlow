package com.r786.studyflow.modules.quiz.dto;

import java.util.List;

public record QuestionRequest(
        String content,
        List<ChoiceRequest> choices
) {}
