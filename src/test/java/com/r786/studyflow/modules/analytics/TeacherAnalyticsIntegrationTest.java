package com.r786.studyflow.modules.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r786.studyflow.modules.auth.entity.Role;
import com.r786.studyflow.modules.auth.entity.Student;
import com.r786.studyflow.modules.auth.entity.Teacher;
import com.r786.studyflow.modules.auth.entity.User;
import com.r786.studyflow.modules.auth.repository.StudentRepository;
import com.r786.studyflow.modules.auth.repository.TeacherRepository;
import com.r786.studyflow.modules.auth.repository.UserRepository;
import com.r786.studyflow.modules.course.entity.Course;
import com.r786.studyflow.modules.course.repository.CourseRepository;
import com.r786.studyflow.modules.quiz.entity.Choice;
import com.r786.studyflow.modules.quiz.entity.Question;
import com.r786.studyflow.modules.quiz.entity.Quiz;
import com.r786.studyflow.modules.quiz.repository.ChoiceRepository;
import com.r786.studyflow.modules.quiz.repository.QuestionRepository;
import com.r786.studyflow.modules.quiz.repository.QuizRepository;
import com.r786.studyflow.modules.course.entity.CourseStudent;
import com.r786.studyflow.modules.course.repository.CourseStudentRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

// CORRECT STATIC IMPORTS
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

// ... (keep your existing imports)
import org.junit.jupiter.api.BeforeEach; // Add this import

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class TeacherAnalyticsIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CourseRepository courseRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private ChoiceRepository choiceRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private CourseStudentRepository enrollmentRepository;

    // Define class-level fields so all methods can see them
    private Teacher manager;
    private Student student;
    private Course course;
    private Quiz quiz;
    private Question question;
    private Choice choice;
    private User teacherUser;

    @BeforeEach
    void setUp() {
        // 1. Setup Teacher
        teacherUser = User.builder()
                .username("teacher_one")
                .firstname("Teacher") // ADD NAMES
                .lastname("One")
                .email("teacher@studyflow.com")
                .password("pass")
                .role(Role.TEACHER)
                .isActive(true)
                .build();
        userRepository.save(teacherUser);

        manager = Teacher.builder()
                .user(teacherUser)
                .department("CSE")
                .designation("Professor")
                .build();
        teacherRepository.save(manager);

        // 2. Setup Student
        User studentUser = User.builder()
                .username("student_one")
                .firstname("student") // ADD NAMES
                .lastname("one")
                .email("student@studyflow.com")
                .password("pass")
                .role(Role.STUDENT)
                .build();
        userRepository.save(studentUser);

        student = Student.builder()
                .user(studentUser)
                .department("CSE")
                .semester(4)
                .build();
        studentRepository.save(student);

        // 3. Setup Course
        course = Course.builder()
                .title("Integration Testing 101")
                .manager(manager)
                .build();
        courseRepository.save(course);

        // 4. CRITICAL: Enroll the student in the course
        CourseStudent enrollment = CourseStudent.builder()
                .course(course)
                .student(student)
                .isEnrolled(true)
                .build();
        enrollmentRepository.save(enrollment);

        // 4. Setup Quiz, Question, and Choice
        quiz = Quiz.builder()
                .title("Unit 1 Quiz")
                .course(course)
                .build();
        quizRepository.save(quiz);

        question = Question.builder()
                .quiz(quiz)
                .content("What is Integration Testing?")
                .points(10)
                .build();
        questionRepository.save(question);

        choice = Choice.builder()
                .question(question)
                .content("Testing modules together")
                .isCorrect(true)
                .build();
        choiceRepository.save(choice);
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void shouldReflectStudentSubmissionInTeacherAnalytics() throws Exception {
        // 1. Submit the quiz as a student
        Map<Long, Long> answers = Map.of(question.getId(), choice.getId());

        mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/submit")
                        .param("studentId", student.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answers)))
                .andExpect(status().isOk());

        // 2. ASSERT: Get Analytics as the Teacher
        // We use .with(authentication(...)) to pass the real teacherUser entity
        // which prevents the NullPointerException in the Controller.
        mockMvc.perform(get("/api/v1/analytics/course/" + course.getId())
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                teacherUser,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + teacherUser.getRole().name()))
                        )))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents", is(1))) // Now should be 1
                .andExpect(jsonPath("$.completionRate", is(100.0))) // Now should be 100%
                .andExpect(jsonPath("$.topPerformers[0].studentName", containsString("student")));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void shouldPreventDuplicateSubmissions() throws Exception {
        Map<Long, Long> answers = Map.of(question.getId(), choice.getId());

        // First submission
        mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/submit")
                        .param("studentId", student.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answers)))
                .andExpect(status().isOk());

        // Second submission
        mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/submit")
                        .param("studentId", student.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answers)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void shouldReturn404ForInvalidChoiceId() throws Exception {
        Map<Long, Long> invalidAnswers = Map.of(question.getId(), 9999L);

        mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/submit")
                        .param("studentId", student.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAnswers)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void shouldHandleEmptySubmissionWithZeroScore() throws Exception {
        Map<Long, Long> emptyAnswers = Map.of();

        mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/submit")
                        .param("studentId", student.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyAnswers)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(0)));
    }
}