package com.r786.studyflow.modules.auth.security;

import com.r786.studyflow.modules.auth.entity.User;
import com.r786.studyflow.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;


@Service
@RequiredArgsConstructor
public class StudyFlowUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly=true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
     User user = userRepository.findByUsername(username)
             .orElseThrow(() -> new UsernameNotFoundException("User not found with username"+username));

     return new StudyFlowUserDetails(user);
    }
}
