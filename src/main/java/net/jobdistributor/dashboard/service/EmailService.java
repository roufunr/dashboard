package net.jobdistributor.dashboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username:rouf@jobdistributor.net}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;

            String subject = "Verify Your Email Address - JobDistributor";
            String content = buildVerificationEmailContent(verificationUrl);

            sendEmail(toEmail, subject, content);
            logger.info("Verification email sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String subject = "Password Reset Request - JobDistributor";
            String content = buildPasswordResetEmailContent(resetUrl);

            sendEmail(toEmail, subject, content);
            logger.info("Password reset email sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            // Don't throw exception here - we don't want to reveal if email exists
        }
    }

    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            String subject = "Welcome to JobDistributor!";
            String content = buildWelcomeEmailContent(firstName);

            sendEmail(toEmail, subject, content);
            logger.info("Welcome email sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    private void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    private String buildVerificationEmailContent(String verificationUrl) {
        return """
            Hi there!
            
            Thank you for signing up for JobDistributor. To complete your registration, 
            please verify your email address by clicking the link below:
            
            %s
            
            This link will expire in 24 hours.
            
            If you didn't create an account, you can safely ignore this email.
            
            Best regards,
            The JobDistributor Team
            """.formatted(verificationUrl);
    }

    private String buildPasswordResetEmailContent(String resetUrl) {
        return """
            Hi there!
            
            We received a request to reset your password for your JobDistributor account.
            Click the link below to reset your password:
            
            %s
            
            This link will expire in 1 hour.
            
            If you didn't request a password reset, you can safely ignore this email.
            
            Best regards,
            The JobDistributor Team
            """.formatted(resetUrl);
    }

    private String buildWelcomeEmailContent(String firstName) {
        return """
            Hi %s!
            
            Welcome to JobDistributor! Your email has been verified successfully.
            
            You can now access your dashboard and start using our platform.
            
            If you have any questions, feel free to contact our support team.
            
            Best regards,
            The JobDistributor Team
            """.formatted(firstName);
    }
}