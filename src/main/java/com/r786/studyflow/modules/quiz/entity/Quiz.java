package com.r786.studyflow.modules.quiz.entity;

import com.r786.studyflow.modules.course.entity.Course;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quizzes", schema = "quiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private String title;
    private String description;
    private Integer timeLimitInMinutes;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @Version // Optimistic Locking
    private Integer version;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
