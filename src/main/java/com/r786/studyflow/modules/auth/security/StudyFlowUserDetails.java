package com.r786.studyflow.modules.auth.security;

import com.r786.studyflow.modules.auth.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class StudyFlowUserDetails implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public StudyFlowUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.active = user.isActive();

        // Check if role is null before calling .name()
        if (user.getRole() != null) {
            this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        } else {
            // Assign no authorities or a default "ROLE_NONE" for unprofiled users
            this.authorities = List.of();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return authorities;
    }

    @Override
    public String getPassword(){
        return password;
    }

    @Override
    public String getUsername(){
        return username;
    }

    @Override
    public boolean isEnabled(){
        return active;
    }
}
