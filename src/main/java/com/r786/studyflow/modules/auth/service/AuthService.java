package com.r786.studyflow.modules.auth.service;

import com.r786.studyflow.modules.auth.dto.AuthenticationRequest;
import com.r786.studyflow.modules.auth.dto.AuthenticationResponse;
import com.r786.studyflow.modules.auth.dto.RegisterRequest;
import com.r786.studyflow.modules.auth.entity.User;
import com.r786.studyflow.modules.auth.repository.UserRepository;
import com.r786.studyflow.modules.auth.security.JwtService;
import com.r786.studyflow.modules.auth.security.StudyFlowUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstname(request.firstname())
                .lastname(request.lastname())
                .role(request.role())
                .isActive(true)
                .build();

        userRepository.save(user);

        var userDetails = new StudyFlowUserDetails(user);
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthenticationResponse(jwtToken, refreshToken);
    }
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        var user = userRepository.findByUsername(request.username())
                .orElseThrow(()->new UsernameNotFoundException(""));
        var userDetails = new StudyFlowUserDetails(user);

        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthenticationResponse(jwtToken, refreshToken);

    }
}
