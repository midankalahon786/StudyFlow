package com.r786.studyflow.modules.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String username, String otp) {
        String subject = "StudyFlow - Verify Your Account";
        String body = String.format(
                "<h3>Welcome to StudyFlow, %s!</h3>" +
                        "<p>Please use the following code to verify your account:</p>" +
                        "<h2 style='color: #2196F3;'>%s</h2>" +
                        "<p>This code will expire in 10 minutes.</p>",
                username, otp
        );
        sendHtmlEmail(to, subject, body);
    }

    // Add this method to resolve the error in AuthService
    @Async
    public void sendPasswordResetEmail(String to, String otp) {
        String subject = "StudyFlow - Reset Your Password";
        String body = String.format(
                "<h3>Password Reset Request</h3>" +
                        "<p>You requested to reset your StudyFlow password. Use the code below to proceed:</p>" +
                        "<h2 style='color: #F44336;'>%s</h2>" + // Red color for reset/security
                        "<p>This code will expire in 15 minutes. If you didn't request this, ignore this email.</p>",
                otp
        );
        sendHtmlEmail(to, subject, body);
    }

    // Generic helper to avoid code duplication
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("noreply@studyflow.com");
            helper.setText(htmlContent, true); // true enables HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Log this in a real scenario
            throw new RuntimeException("Failed to send HTML email to: " + to);
        }
    }
    @Async
    public void sendEmailUpdateOldEmailOtp(String to, String username, String otp) {
        String subject = "StudyFlow - Security Alert: Email Change Requested";
        String body = String.format(
                "<h3>Hello %s,</h3>" +
                        "<p>A request has been made to change your StudyFlow account email. " +
                        "To proceed, please enter the following code to verify your current email address:</p>" +
                        "<h2 style='color: #2196F3;'>%s</h2>" +
                        "<p>If you did not initiate this request, please change your password immediately.</p>",
                username, otp
        );
        sendHtmlEmail(to, subject, body);
    }

    @Async
    public void sendEmailUpdateNewEmailOtp(String to, String username, String otp) {
        String subject = "StudyFlow - Verify Your New Email Address";
        String body = String.format(
                "<h3>Finalize Your Email Change, %s!</h3>" +
                        "<p>You are almost there. Use the code below to verify that this is your new email address:</p>" +
                        "<h2 style='color: #4CAF50;'>%s</h2>" + // Green for confirmation/success
                        "<p>Once verified, your account email will be updated permanently.</p>",
                username, otp
        );
        sendHtmlEmail(to, subject, body);
    }
}
