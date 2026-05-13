package com.r786.studyflow.modules.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "global_logs", schema = "audit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String principal; // Who did it (username or "SYSTEM")
    private String module;    // Which module (Auth, Course, Quiz)
    private String action;    // The action type
    private String metadata;  // JSON or String of arguments/results
    private String ipAddress;
    private String status;
    private String details;
    private LocalDateTime timestamp;
    private boolean success;
}
