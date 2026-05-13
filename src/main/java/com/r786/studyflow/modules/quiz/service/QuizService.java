package com.r786.studyflow.modules.quiz.service;

import com.r786.studyflow.core.exceptions.GlobalExceptionHandler;
import com.r786.studyflow.modules.auth.repository.StudentRepository;
import com.r786.studyflow.modules.course.repository.CourseRepository;
import com.r786.studyflow.modules.quiz.dto.QuizRequest;
import com.r786.studyflow.modules.quiz.entity.Choice;
import com.r786.studyflow.modules.quiz.entity.Question;
import com.r786.studyflow.modules.quiz.entity.Quiz;
import com.r786.studyflow.modules.quiz.entity.QuizSubmission;
import com.r786.studyflow.modules.quiz.repository.ChoiceRepository;
import com.r786.studyflow.modules.quiz.repository.QuizRepository;
import com.r786.studyflow.modules.quiz.repository.QuizSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository submissionRepository;
    private final ChoiceRepository choiceRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public QuizSubmission submitQuiz(Long quizId, Long studentId, Map<Long, Long> answers) {
        // 1. Fetch dependencies
        if (submissionRepository.existsByQuizIdAndStudentId(quizId, studentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Quiz already submitted.");
        }
        var quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found"));

        // FIX: Retrieve the student entity from the repository
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));

        // 2. Calculate score
        int score = 0;
        for (Map.Entry<Long, Long> entry : answers.entrySet()) {
            var choice = choiceRepository.findById(entry.getValue())
                    .orElseThrow(() -> new NoSuchElementException("Choice not found"));

            // Change getIsCorrect() to isCorrect()
            if (choice.isCorrect()) {
                score++;
            }
        }

        // 3. Build and save the submission
        var submission = QuizSubmission.builder()
                .quiz(quiz)
                .student(student) // FIX: Properly link the student entity
                .score(score)
                .submittedAt(LocalDateTime.now())
                .build();

        return submissionRepository.save(submission);
    }

    @Transactional
    public Quiz createQuiz(QuizRequest request) {
        // 1. Validate course existence
        var course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Course not found"));

        // 2. Build Quiz Entity
        Quiz quiz = Quiz.builder()
                .course(course)
                .title(request.title())
                .description(request.description())
                .timeLimitInMinutes(request.timeLimitInMinutes())
                .build();

        // 3. Map Questions and Choices
        List<Question> questions = request.questions().stream().map(qReq -> {
            Question question = Question.builder()
                    .content(qReq.content())
                    .quiz(quiz)
                    .build();

            List<Choice> choices = qReq.choices().stream().map(cReq ->
                    Choice.builder()
                            .content(cReq.content())
                            .isCorrect(cReq.isCorrect())
                            .question(question)
                            .build()
            ).collect(Collectors.toList());

            question.setChoices(choices);
            return question;
        }).collect(Collectors.toList());

        quiz.setQuestions(questions);

        // 4. Save hierarchy (Ensuring CascadeType.ALL is on in entities)
        return quizRepository.save(quiz);
    }

    @Transactional(readOnly = true)
    public Quiz getQuizForStudent(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Quiz not found"));

        // Security: Strip 'isCorrect' flags before sending to student
        // This prevents students from inspecting the JSON response to find answers
        quiz.getQuestions().forEach(question ->
                question.getChoices().forEach(choice -> choice.setCorrect(false))
        );

        return quiz;
    }

    @Transactional
    public Quiz updateQuiz(Long quizId, QuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Quiz not found"));

        // Update metadata
        quiz.setTitle(request.title());
        quiz.setDescription(request.description());
        quiz.setTimeLimitInMinutes(request.timeLimitInMinutes());

        // For a monolith-to-microservices transition, we maintain high data integrity here
        // Clear existing questions and re-add from request to handle full structural updates
        quiz.getQuestions().clear();

        request.questions().forEach(qReq -> {
            Question question = Question.builder()
                    .content(qReq.content())
                    .quiz(quiz)
                    .build();

            List<Choice> choices = qReq.choices().stream().map(cReq ->
                    Choice.builder()
                            .content(cReq.content())
                            .isCorrect(cReq.isCorrect())
                            .question(question)
                            .build()
            ).collect(Collectors.toList());

            question.setChoices(choices);
            quiz.getQuestions().add(question);
        });

        return quizRepository.save(quiz);
    }
}
