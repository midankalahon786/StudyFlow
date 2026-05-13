package com.r786.studyflow.modules.audit.controller;

import com.r786.studyflow.modules.audit.entity.GlobalAuditLog;
import com.r786.studyflow.modules.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository repository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Only you should see this!
    public List<GlobalAuditLog> getAllLogs() {
        return repository.findAll();
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<GlobalAuditLog> getLogsByUser(@PathVariable String username) {
        return repository.findByPrincipalOrderByTimestampDesc(username);
    }
}