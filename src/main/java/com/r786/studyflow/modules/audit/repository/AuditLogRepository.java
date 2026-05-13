package com.r786.studyflow.modules.audit.repository;
import com.r786.studyflow.modules.audit.entity.GlobalAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<GlobalAuditLog, Long> {

    // You can add custom query methods here later for your Analytics Dashboard
    // Example: Find logs for a specific user
    List<GlobalAuditLog> findByPrincipalOrderByTimestampDesc(String principal);

    // Example: Find logs by module (AUTH, COURSE, etc.)
    List<GlobalAuditLog> findByModule(String module);
}
