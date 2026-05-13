package com.r786.studyflow.modules.audit.service;

import com.r786.studyflow.modules.audit.entity.AuditAction;
import com.r786.studyflow.modules.audit.entity.GlobalAuditLog;
import com.r786.studyflow.modules.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async("taskExecutor")
    public void log(
            String username,
            String module,
            AuditAction action,
            String status,
            String details,
            String ipAddress // Passed from the caller thread
    ) {
        GlobalAuditLog auditLog = GlobalAuditLog.builder()
                .principal(username != null ? username : "ANONYMOUS")
                .module(module)
                .action(String.valueOf(action))
                .status(status)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }
}