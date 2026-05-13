package com.r786.studyflow.modules.audit.annotation;

import com.r786.studyflow.modules.audit.entity.AuditAction;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditTrail {
    AuditAction action();
    String description() default "";
}

