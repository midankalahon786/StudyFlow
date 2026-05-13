package com.r786.studyflow.modules.auth.repository;

import com.r786.studyflow.modules.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByOtp(String otp);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
