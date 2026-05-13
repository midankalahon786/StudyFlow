package com.r786.studyflow.modules.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

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
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "firstname") // Force mapping to the lowercase column in your screenshot
    private String firstName;

    @Column(name = "lastname") // Force mapping to the lowercase column in your screenshot
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phonenumber;
    private LocalDate dateOfBirth;

    @Column(name = "is_active")
    private boolean isActive = false;

    @Column(name = "otp")
    private String otp;

    private LocalDateTime otpExpiry;

    @Version
    private Integer version;


    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "temp_email")
    private String tempEmail;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Defensive check to handle users who haven't completed their profile
        if (role == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public boolean isEnabled() { return isActive; }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Or logic based on your needs
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

}
