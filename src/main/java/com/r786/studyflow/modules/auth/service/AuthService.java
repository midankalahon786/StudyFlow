package com.r786.studyflow.modules.auth.service;

import com.r786.studyflow.core.utils.OtpUtils;
import com.r786.studyflow.modules.audit.annotation.AuditTrail;
import com.r786.studyflow.modules.audit.entity.AuditAction;
import com.r786.studyflow.modules.auth.dto.*;
import com.r786.studyflow.modules.auth.entity.Role;
import com.r786.studyflow.modules.auth.entity.Student;
import com.r786.studyflow.modules.auth.entity.Teacher;
import com.r786.studyflow.modules.auth.entity.User;
import com.r786.studyflow.modules.auth.repository.StudentRepository;
import com.r786.studyflow.modules.auth.repository.TeacherRepository;
import com.r786.studyflow.modules.auth.repository.UserRepository;
import com.r786.studyflow.modules.auth.security.JwtService;
import com.r786.studyflow.modules.auth.security.StudyFlowUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    private final AuthenticationManager authenticationManager;

    // PHASE 1: Create Account (Disabled) & Trigger OTP
    @AuditTrail(action = AuditAction.REGISTER_INITIATED, description = "New registration started or resumed")
    public void initiateRegistration(AuthenticationRequest request) {
        // 1. Check for username existence
        var existingUser = userRepository.findByUsername(request.username());

        if (existingUser.isPresent()) {
            // If the user is already active, they cannot register again
            if (existingUser.get().isActive()) {
                throw new RuntimeException("Username already taken and account is active");
            }

            // If they exist but are NOT active, we update the existing record
            updateInactiveUser(existingUser.get(), request);
        } else {
            // 2. Check for email existence separately (if username is unique, email might still be taken)
            if (userRepository.existsByEmail(request.email())) {
                throw new RuntimeException("Email already registered to another account");
            }

            // 3. Create a brand new user
            createNewUser(request);
        }
    }

    private void createNewUser(AuthenticationRequest request) {
        String generatedOtp = OtpUtils.generateOtp(6);
        var user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .isActive(false)
                .otp(generatedOtp)
                .otpExpiry(LocalDateTime.now().plusMinutes(10))
                .build();

        userRepository.save(user);
        emailService.sendVerificationEmail(request.email(), request.username(), generatedOtp);
    }

    private void updateInactiveUser(User user, AuthenticationRequest request) {
        String generatedOtp = OtpUtils.generateOtp(6);

        // Update fields in case the user changed their password/email attempt
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setOtp(generatedOtp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);
        emailService.sendVerificationEmail(request.email(), request.username(), generatedOtp);
    }

    // PHASE 2: Verify OTP & Return Temporary Token
    @AuditTrail(action = AuditAction.OTP_VERIFIED, description = "User submitted OTP for verification")
    public AuthenticationResponse verifyOtp(String otp, String username) {
        // 1. Find the specific user first
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        System.out.println("DEBUG: DB OTP is [" + user.getOtp() + "]");
        System.out.println("DEBUG: Input OTP is [" + otp + "]");

        // 2. Validate the OTP exists and matches
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        // 3. Check for expiration (Assuming you have an otpExpiry field)
        if (user.getOtpExpiry() != null && user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        // 4. Success! Clear the OTP so it can't be reused
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        // 5. Generate a temporary "Pre-Profile" token
        var userDetails = new StudyFlowUserDetails(user);
        var tempToken = jwtService.generateToken(userDetails);

        // Return the object so the Android app receives the JSON token!
        return new AuthenticationResponse(tempToken, null);
    }

    // PHASE 3: Complete Profile & Activate Account
    @Transactional
    @AuditTrail(action = AuditAction.REGISTRATION_COMPLETED, description = "User registration completed")
    public AuthenticationResponse completeProfile(RegisterRequest request, String currentUsername) {
        var user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 1. Match the capital 'N' for camelCase fields
        user.setFirstName(request.firstName()); // Check if DTO uses firstName() or firstname()
        user.setLastName(request.lastName());
        user.setEmail(request.email());

        // Ensure this field in the User entity is named 'role' (matching Role enum)
        user.setRole(request.role());

        // If your field is 'isActive', Lombok usually generates 'setActive'
        user.setActive(true);

        user.setOtp(null);
        userRepository.save(user);

        // 2. Sub-Profile Logic (This looks solid)
        if (request.role() == Role.TEACHER) {
            Teacher teacher = Teacher.builder()
                    .user(user)
                    .department(request.department())
                    .build();
            teacherRepository.save(teacher);
        } else if (request.role() == Role.STUDENT) {
            Student student = Student.builder()
                    .user(user)
                    .enrollmentNo(request.enrollmentNo())
                    .semester(request.semester())
                    .build();
            studentRepository.save(student);
        }

        var userDetails = new StudyFlowUserDetails(user);
        return new AuthenticationResponse(
                jwtService.generateToken(userDetails),
                jwtService.generateRefreshToken(userDetails)
        );
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> UserResponse.builder()
                        .id(Math.toIntExact(user.getId())) // Use Long here if possible
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build())
                .toList();
    }

    @AuditTrail(action = AuditAction.USER_AUTHENTICATED, description = "User is authenticated")
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // Change 'identifier' to 'request.username()'
        var user = userRepository.findByUsernameOrEmail(request.username(), request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with: " + request.username()));

        var userDetails = new StudyFlowUserDetails(user);
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthenticationResponse(jwtToken, refreshToken);
    }

    @AuditTrail(action = AuditAction.PASSWORD_RESET_REQUESTED, description = "User requested password reset")
    public void initiatePasswordReset(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        String resetOtp = OtpUtils.generateOtp(6);
        user.setOtp(resetOtp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(email, resetOtp);
    }

    @Transactional
    @AuditTrail(action = AuditAction.PASSWORD_RESET_COMPLETED, description = "User successfully reset password")
    public void resetPassword(ResetPasswordRequest request) {
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() == null || !user.getOtp().equals(request.otp())) {
            throw new RuntimeException("Invalid reset code");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset code expired");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setOtp(null); // Clear OTP after success
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    @AuditTrail(action = AuditAction.PROFILE_UPDATED, description = "User updated their profile information")
    public void updateProfile(UpdateProfileRequest request, String currentUsername) {
        // 1. Fetch the base User entity
        var user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Update central User fields
        // These match the @Column(name = "firstname") mapping fixed for your PostgreSQL schema
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        userRepository.save(user);

        // 3. Update Sub-Profile based on Role (Class Table Inheritance)
        if (user.getRole() == Role.TEACHER) {
            var teacher = teacherRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Teacher profile not found"));
            teacher.setDepartment(request.department());
            teacherRepository.save(teacher);

        } else if (user.getRole() == Role.STUDENT) {
            var student = studentRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Student profile not found"));
            student.setEnrollmentNo(request.enrollmentNo());
            student.setSemester(request.semester());
            studentRepository.save(student);
        }
    }

    // PHASE 1: Verify Identity via Current Email
    @AuditTrail(action = AuditAction.EMAIL_UPDATE_INITIATED, description = "Initiated email change request")
    public void initiateEmailUpdate(String newEmail, String currentUsername) {
        var user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 1. Check if the new email is already taken in PostgreSQL
        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("This email is already registered to another account");
        }

        // 2. Generate OTP for the OLD email (Security check)
        String otp = OtpUtils.generateOtp(6);
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        user.setTempEmail(newEmail); // Store the target email temporarily
        userRepository.save(user);

        // 3. Notify the current inbox
        emailService.sendEmailUpdateOldEmailOtp(user.getEmail(), user.getUsername(), otp);
    }

    // PHASE 2: Verify Current Email & Trigger New Email OTP
    @AuditTrail(action = AuditAction.EMAIL_UPDATE_OLD_VERIFIED, description = "Current email ownership verified")
    public void verifyOldEmailAndSendNewOtp(String otp, String currentUsername) {
        var user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Validate the first OTP
        if (user.getOtp() == null || !user.getOtp().equals(otp) ||
                user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired OTP for the current email");
        }

        // 1. Generate a second OTP for the NEW email (Typos/Existence check)
        String newOtp = OtpUtils.generateOtp(6);
        user.setOtp(newOtp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // 2. Send to the pending tempEmail address
        emailService.sendEmailUpdateNewEmailOtp(user.getTempEmail(), user.getUsername(), newOtp);
    }

    // PHASE 3: Finalize and Update
    @Transactional
    @AuditTrail(action = AuditAction.EMAIL_UPDATE_COMPLETED, description = "New email verified and updated")
    public void finalizeEmailUpdate(String otp, String currentUsername) {
        var user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Validate the second OTP
        if (user.getOtp() == null || !user.getOtp().equals(otp) ||
                user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired OTP for the new email");
        }

        // 1. Officially update the record in the 'auth.users' table
        user.setEmail(user.getTempEmail());

        // 2. Cleanup
        user.setTempEmail(null);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    public AuthenticationResponse refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid refresh token");
        }

        String refreshToken = authHeader.substring(7);
        String username = jwtService.extractUsername(refreshToken);

        if (username != null) {
            var user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            var userDetails = new StudyFlowUserDetails(user);

            // Check if the refresh token is still valid
            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                var accessToken = jwtService.generateToken(userDetails);
                // Optional: Issue a new refresh token as well (token rotation)
                var newRefreshToken = jwtService.generateRefreshToken(userDetails);

                return new AuthenticationResponse(accessToken, newRefreshToken);
            }
        }
        throw new RuntimeException("Token refresh failed");
    }
}
