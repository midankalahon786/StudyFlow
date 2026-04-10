package com.r786.studyflow.modules.course.entity;

import com.r786.studyflow.modules.auth.entity.Teacher;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="courses", schema = "courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String department;

    // 1. Changed 'teacher' to 'manager' to match your query
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="manager_id", nullable = false)
    private Teacher manager;

    // 2. Added Associated Teachers relationship
    @ManyToMany
    @JoinTable(
            name = "course_associated_teachers",
            schema = "courses",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private Set<Teacher> associatedTeachers = new HashSet<>();

    // 3. Added the Enrollments relationship (This fixes the specific error)
    @OneToMany(mappedBy = "course")
    private List<CourseStudent> enrollments;

    @Builder.Default
    private Integer noOfStudentsEnrolled = 0;

    @Version
    private Integer version;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
