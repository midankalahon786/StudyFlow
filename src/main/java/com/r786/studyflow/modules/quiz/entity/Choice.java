package com.r786.studyflow.modules.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "choices", schema = "quiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Choice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private String content;
    private boolean isCorrect; // Used for automated grading
}
