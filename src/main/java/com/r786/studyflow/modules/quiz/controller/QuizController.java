package com.r786.studyflow.modules.quiz.controller;

import com.r786.studyflow.modules.quiz.dto.QuizRequest;
import com.r786.studyflow.modules.quiz.entity.Quiz;
import com.r786.studyflow.modules.quiz.entity.QuizSubmission;
import com.r786.studyflow.modules.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quiz Module", description = "Endpoints for quiz creation, management, and automated grading")
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "Create a new quiz", description = "Allows teachers to define a quiz with a question bank and correct answers.")
    @PostMapping
    public ResponseEntity<Quiz> createQuiz(@RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.createQuiz(request));
    }

    @Operation(summary = "Fetch quiz for students", description = "Retrieves quiz details and choices. Note: isCorrect flags are hidden from this view.")
    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizForStudent(id));
    }

    @Operation(summary = "Submit quiz for grading", description = "Automated grading logic calculates the score based on submitted choice IDs.")
    @PostMapping("/{id}/submit")
    public ResponseEntity<QuizSubmission> submitQuiz(
            @PathVariable Long id,
            @RequestParam Long studentId,
            @RequestBody Map<Long, Long> answers) {
        return ResponseEntity.ok(quizService.submitQuiz(id, studentId, answers));
    }
}