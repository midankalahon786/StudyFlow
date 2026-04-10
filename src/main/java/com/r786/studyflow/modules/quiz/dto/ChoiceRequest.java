package com.r786.studyflow.modules.quiz.dto;

public record ChoiceRequest(
        String content,
        boolean isCorrect
) {}
