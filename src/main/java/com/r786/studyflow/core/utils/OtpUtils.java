package com.r786.studyflow.core.utils;

import java.security.SecureRandom;

public class OtpUtils {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateOtp(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(secureRandom.nextInt(10)); // Generates 0-9
        }
        return otp.toString();
    }
}
