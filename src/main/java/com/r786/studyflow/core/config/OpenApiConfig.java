package com.r786.studyflow.core.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi authApi(){
        return GroupedOpenApi.builder()
                .group("1-Authentication-Service")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi courseApi(){
        return GroupedOpenApi.builder()
                .group("2-Course-Management-Service")
                .pathsToMatch("/api/v1/enrollments/**","/api/v1/courses/**","/api/v1/resources/**")
                .build();
    }

    @Bean
    public GroupedOpenApi quizApi(){
        return GroupedOpenApi.builder()
                .group("3-Quiz-Service")
                .pathsToMatch("/api/v1/quizzes/**","/api/v1/questions/**")
                .build();
    }
}
