package com.r786.studyflow.core.utils;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {
    public static String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getHeader("X-Forwarded-For");
        if (remoteAddr == null || remoteAddr.isEmpty()) {
            remoteAddr = request.getRemoteAddr();
        }
        return remoteAddr;
    }
}
