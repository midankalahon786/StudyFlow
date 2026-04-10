package com.r786.studyflow.modules.analytics.service;

import com.r786.studyflow.core.exceptions.GlobalExceptionHandler;
import com.r786.studyflow.modules.analytics.dto.StudentPerformanceResponse;
import com.r786.studyflow.modules.analytics.dto.TeacherAnalyticsResponse;
import com.r786.studyflow.modules.course.repository.CourseRepository;
import com.r786.studyflow.modules.quiz.entity.Quiz;
import com.r786.studyflow.modules.quiz.entity.QuizSubmission;
import com.r786.studyflow.modules.quiz.repository.QuizRepository;
import com.r786.studyflow.modules.quiz.repository.QuizSubmissionRepository;
import com.r786.studyflow.modules.course.repository.CourseStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final QuizSubmissionRepository submissionRepository;
    private final CourseStudentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final QuizRepository quizRepository;

    @Transactional(readOnly = true)
    public StudentPerformanceResponse getStudentPerformance(Long studentId) {
        // 1. Fetch all submissions for the student
        var submissions = submissionRepository.findByStudentId(studentId);

        // 2. Calculate average score
        double average = submissions.stream()
                .mapToInt(QuizSubmission::getScore)
                .average()
                .orElse(0.0);

        // 3. Map Quiz Titles to Scores
        var results = submissions.stream()
                .collect(Collectors.toMap(
                        s -> s.getQuiz().getTitle(),
                        QuizSubmission::getScore,
                        (existing, replacement) -> replacement // Handle multiple attempts
                ));

        // 4. Fetch enrolled course titles
        var courses = enrollmentRepository.findAllByStudentId(studentId).stream()
                .map(e -> e.getCourse().getTitle())
                .collect(Collectors.toList());

        return new StudentPerformanceResponse(average, submissions.size(), results, courses);
    }

    @Transactional(readOnly = true)
    public TeacherAnalyticsResponse getCourseAnalytics(Long courseId, Long teacherId) {
        // 1. Authority Check
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Course not found"));

        boolean isStaff = course.getManager().getId().equals(teacherId) ||
                course.getAssociatedTeachers().stream()
                        .anyMatch(t -> t.getId().equals(teacherId));

        if (!isStaff) {
            throw new RuntimeException("Access Denied: Not authorized for this course.");
        }

        // 2. Aggregate Enrollment and Quizzes
        int studentCount = enrollmentRepository.findAllByCourseId(courseId).size();
        var quizzes = quizRepository.findByCourseId(courseId);

        // 3. Calculate Class Averages per Quiz
        Map<String, Double> averages = quizzes.stream()
                .collect(Collectors.toMap(
                        Quiz::getTitle,
                        q -> {
                            Double avg = submissionRepository.findAverageScoreByQuizId(q.getId());
                            return (avg != null) ? Math.round(avg * 100.0) / 100.0 : 0.0;
                        }
                ));

        // 4. Calculate Overall Completion Rate
        long totalActualSubmissions = quizzes.stream()
                .mapToLong(q -> submissionRepository.countByQuizId(q.getId()))
                .sum();

        double potentialSubmissions = (double) quizzes.size() * studentCount;
        double completionRate = (potentialSubmissions > 0)
                ? (totalActualSubmissions / potentialSubmissions) * 100
                : 0.0;

        return new TeacherAnalyticsResponse(
                studentCount,
                averages,
                null, // Top performers can be added later as a secondary feature
                Math.round(completionRate * 100.0) / 100.0
        );
    }
}