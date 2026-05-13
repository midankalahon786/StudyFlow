package com.r786.studyflow.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Customize these based on your PC (RTX 3050 / 32GB RAM is plenty!)
        executor.setCorePoolSize(5);      // 5 threads always ready
        executor.setMaxPoolSize(10);     // Can scale up to 10
        executor.setQueueCapacity(500);  // Waitlist for tasks
        executor.setThreadNamePrefix("StudyFlow-Async-");
        executor.initialize();

        return executor;
    }
}