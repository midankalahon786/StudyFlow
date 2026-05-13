package com.r786.studyflow.modules.audit.service;

import com.r786.studyflow.core.utils.RequestUtils;
import com.r786.studyflow.modules.audit.annotation.AuditTrail;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final HttpServletRequest request;

    @Around("@annotation(auditTrail)")
    public Object profile(ProceedingJoinPoint joinPoint, AuditTrail auditTrail) throws Throwable {
        // 1. Get the current IP from the request (Main Thread)
        String ipAddress = RequestUtils.getClientIp(request);

        // 2. Get the current user
        String username = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "GUEST";

        // 3. Determine the module name (e.g., "AuthService" or "CourseService")
        String moduleName = joinPoint.getTarget().getClass().getSimpleName();

        String methodName = joinPoint.getSignature().toShortString();

        Object result;
        try {
            result = joinPoint.proceed(); // Execute the actual business logic

            // 4. Call log with all 6 arguments:
            // username, module, action, status, details, ipAddress
            auditService.log(
                    username,
                    moduleName,
                    auditTrail.action(),
                    "SUCCESS",
                    "Executed: " + methodName,
                    ipAddress
            );

            return result;
        } catch (Throwable e) {
            // 4 (Fallback). Log the failure
            auditService.log(
                    username,
                    moduleName,
                    auditTrail.action(),
                    "FAILURE",
                    "Failed: " + e.getMessage(),
                    ipAddress
            );
            throw e;
        }
    }
}