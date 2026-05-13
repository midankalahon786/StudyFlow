package com.r786.studyflow.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("StudyFlow LMS API")
                        .version("1.0")
                        .description("Comprehensive API for course management, automated quizzes, and student analytics.")
                        .contact(new Contact().name("Midanka Lahon").email("midankalahon@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

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
                .pathsToMatch("/api/v1/enrollments/**","/api/v1/courses/**","/api/v1/resources/**", "/api/v1/admin/courses/**")
                .build();
    }

    @Bean
    public GroupedOpenApi quizApi(){
        return GroupedOpenApi.builder()
                .group("3-Quiz-Service")
                .pathsToMatch("/api/v1/quizzes/**","/api/v1/questions/**")
                .build();
    }

    @Bean
    public GroupedOpenApi discussionApi(){
        return GroupedOpenApi.builder()
                .group("4-Discussion-Service")
                .pathsToMatch("/api/v1/discussion/**")
                .build();
    }

    @Bean
    public GroupedOpenApi analyticsApi(){
        return GroupedOpenApi.builder()
                .group("5-Analytics-Service")
                .pathsToMatch("/api/v1/analytics/**")
                .build();
    }
}