package com.covira.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    // OTP email
    public void sendOtpEmail(String to, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("mailUsername");
        message.setTo(to);
        message.setSubject("Covira Password Reset OTP");

        message.setText(
                "Hello,\n\n" +
                        "Your Covira password reset verification code is:\n\n" +
                        otp +
                        "\n\nThis code will expire in 5 minutes.\n\n" +
                        "If you did not request this password reset, please ignore this email.\n\n" +
                        "Regards,\n" +
                        "Covira Team"
        );

        mailSender.send(message);
    }
    public void sendWelcomeEmail(String toEmail, String fullName) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("🎉 Welcome to Covira");

        message.setText(
                "Hello " + fullName + ",\n\n" +

                        "Welcome to Covira!\n\n" +

                        "Your employer account has been created successfully.\n\n" +

                        "You can now:\n" +
                        "• Create video interviews\n" +
                        "• Manage interview questions\n" +
                        "• Invite candidates\n" +
                        "• Review candidate responses\n\n" +

                        "Login here:\n" +
                        "http://localhost:5173/login\n\n" +

                        "Thank you for choosing Covira.\n\n" +

                        "Regards,\n" +
                        "The Covira Team"
        );

        mailSender.send(message);
    }
    public void sendChangePasswordOtpEmail(String to, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("mailUsername");
        message.setTo(to);
        message.setSubject("Covira Password Change Verification");

        message.setText(
                "Hello,\n\n" +
                        "You requested to change your Covira account password.\n\n" +
                        "Your verification code is:\n\n" +
                        otp +
                        "\n\nThis code will expire in 5 minutes.\n\n" +
                        "If you did not request this change, please secure your account immediately.\n\n" +
                        "Regards,\n" +
                        "Covira Team"
        );

        mailSender.send(message);
    }
        // Sent after a password change has been fully verified and applied.
    // Never includes the new password or the OTP.
    public void sendPasswordChangeConfirmationEmail(String to, LocalDateTime changedAt) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm");

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("mailUsername");
        message.setTo(to);
        message.setSubject("Your Covira account details have been successfully updated");

        message.setText(
                "Hello,\n\n" +
                        "Your Covira account details have been successfully updated.\n\n" +
                        "Account: " + to + "\n" +
                        "Change: Password updated\n" +
                        "Date and time: " + changedAt.format(formatter) + "\n\n" +
                        "If you did not make this change, please contact our support team " +
                        "immediately and change your password as soon as possible.\n\n" +
                        "Regards,\n" +
                        "Covira Team"
        );

        mailSender.send(message);
    }

    // Sent after a general profile update (company name, contact person, email, phone).
    // Deliberately does not include the old/new field values.
    public void sendProfileUpdateConfirmationEmail(String to, LocalDateTime updatedAt) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm");

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("mailUsername");
        message.setTo(to);
        message.setSubject("Your Covira account details have been successfully updated");

        message.setText(
                "Hello,\n\n" +
                        "Your Covira account details have been successfully updated.\n\n" +
                        "Account: " + to + "\n" +
                        "Change: Company profile information updated\n" +
                        "Date and time: " + updatedAt.format(formatter) + "\n\n" +
                        "If you did not make this change, please contact our support team " +
                        "immediately.\n\n" +
                        "Regards,\n" +
                        "Covira Team"
        );

        mailSender.send(message);
    }
}