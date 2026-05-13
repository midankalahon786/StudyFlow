package com.r786.studyflow.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // Good practice for DTOs
public class TopPerformerDTO {
    private String studentName;
    private Long totalScore; // FIX: Change from Integer to Long
}