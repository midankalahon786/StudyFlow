package com.r786.studyflow.modules.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name="users",
        schema = "auth",
        uniqueConstraints = {
                @UniqueConstraint(name="uq_user_email", columnNames = "email"),
                @UniqueConstraint(name="uq_user_username", columnNames = "username")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String firstname;
    private String lastname;

    @Column(unique = true, nullable = false)
    private String email;

    private String phonenumber;
    private LocalDate dateOfBirth;

    @Builder.Default
    private boolean isActive = true;

    @Version
    private Integer version;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
