package com.r786.studyflow.modules.auth.controller;

import com.r786.studyflow.modules.auth.dto.*;
import com.r786.studyflow.modules.auth.entity.User;
import com.r786.studyflow.modules.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register/init")
    public ResponseEntity<Void> registerInitial(@RequestBody AuthenticationRequest request) {
        authService.initiateRegistration(request);
        return ResponseEntity.accepted().build();
    }

    // STEP 2: Verify OTP
    // Returns a temporary JWT that allows access to the 'complete-profile' endpoint
    @PostMapping("/register/verify")
    public ResponseEntity<AuthenticationResponse> verifyOtp(@RequestParam String otp, @RequestParam String username) {
        // 1. Get the response from the service
        AuthenticationResponse response = authService.verifyOtp(otp, username);

        // 2. Wrap it in the ResponseEntity so it gets sent as JSON
        return ResponseEntity.ok(response);
    }

    // STEP 3: Complete Profile
    // Receives full details, updates status to 'ACTIVE', returns final JWT with Role
    @PutMapping("/register/complete")
    public ResponseEntity<AuthenticationResponse> completeProfile(
            @RequestBody RegisterRequest request,
            Principal principal // Spring will automatically inject the user from the JWT
    ) {
        // principal.getName() will give you the username extracted from the token
        return ResponseEntity.ok(authService.completeProfile(request, principal.getName()));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        return ResponseEntity.ok(authService.refreshToken(authHeader));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> findAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PostMapping("/forgot-password")
    public void forgot(@RequestBody ForgotPasswordRequest request) {
        authService.initiatePasswordReset(request.email());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateProfileRequest request) {
        authService.updateProfile(request, user.getUsername());
        return ResponseEntity.ok("Profile updated successfully");
    }

    @PostMapping("/profile/email/initiate")
    public ResponseEntity<Void> initiateEmail(@AuthenticationPrincipal User user, @RequestParam String newEmail) {
        authService.initiateEmailUpdate(newEmail, user.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile/email/verify-old")
    public ResponseEntity<Void> verifyOld(@AuthenticationPrincipal User user, @RequestParam String otp) {
        authService.verifyOldEmailAndSendNewOtp(otp, user.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile/email/finalize")
    public ResponseEntity<Void> finalizeEmail(@AuthenticationPrincipal User user, @RequestParam String otp) {
        authService.finalizeEmailUpdate(otp, user.getUsername());
        return ResponseEntity.ok().build();
    }

}
