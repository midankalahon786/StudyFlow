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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

// CORRECT STATIC IMPORTS
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

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

    @Test
    @WithMockUser(roles = "TEACHER")
    void shouldReflectStudentSubmissionInTeacherAnalytics() throws Exception {
        User user = User.builder()
                .username("teacher_one") // FIX: Add the missing username
                .email("teacher@studyflow.com")
                .password("pass")
                .firstname("Teacher")
                .lastname("User")
                .role(Role.valueOf("TEACHER"))
                .isActive(true)
                .build();

        user = userRepository.save(user); // Now this should succeed

        User studentUser = User.builder()
                .username("student_one")
                .email("student@studyflow.com")
                .password("pass")
                .role(Role.valueOf("STUDENT"))
                .build();
        userRepository.save(studentUser);

        Student student = Student.builder()
                .user(studentUser)
                .department("CSE")
                .semester(4)
                .build();
        student = studentRepository.save(student);

        Teacher manager = Teacher.builder()
                .user(user)
                .department("CSE")
                .designation("Professor")
                .build();
        teacherRepository.save(manager);

        // 2. SETUP: Course & Quiz
        Course course = Course.builder()
                .title("Integration Testing 101")
                .manager(manager) // Foreign Key now points to an existing record
                .build();
        courseRepository.save(course);

        Quiz quiz = Quiz.builder()
                .title("Unit 1 Quiz")
                .course(course)
                .build();
        quizRepository.save(quiz);

        Question question = Question.builder()
                .quiz(quiz)
                .content("What is Integration Testing?")
                .points(10)
                .build();
        question = questionRepository.save(question);

        // 2. SETUP: Create a Choice for that Question
        Choice choice = Choice.builder()
                .question(question)
                .content("Testing modules together")
                .isCorrect(true)
                .build();
        choice = choiceRepository.save(choice);

        Map<Long, Long> answers = Map.of(question.getId(), choice.getId());

        mockMvc.perform(post("/api/v1/quizzes/" + quiz.getId() + "/submit")
                        .param("studentId", student.getId().toString()) // Use the saved ID, not "100"
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answers)))
                .andExpect(status().isOk());

        // 4. ASSERT: Analytics
        mockMvc.perform(get("/api/v1/analytics/course/" + course.getId())
                        .param("teacherId", manager.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.classAverages['" + quiz.getTitle() + "']", is(notNullValue())));
    }
}