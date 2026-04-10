package com.r786.studyflow.modules.discussion.entity;

import com.r786.studyflow.modules.auth.entity.User;
import com.r786.studyflow.modules.course.entity.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "discussion_threads", schema = "discussion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "The main topic of a discussion")
public class DiscussionThread {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "The course this discussion belongs to")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author; // Links to central User entity

    private String title;

    @Schema(description = "Initial post contents")
    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @CreationTimestamp
    private LocalDateTime createdAt;
}