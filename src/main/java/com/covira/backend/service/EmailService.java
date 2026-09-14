package com.covira.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // OTP email
    public void sendOtpEmail(String to, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("covirateams@gmail.com");
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
        message.setSubject("Welcome to Covira");

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

        message.setFrom("covirateams@gmail.com");
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

        message.setFrom("covirateams@gmail.com");
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

        message.setFrom("covirateams@gmail.com");
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

    // Sent to a candidate immediately after they submit an interview.
    // Lets them know their response is under review and gives them the
    // 15-day "no response = unsuccessful" expectation.
    public void sendCandidateSubmissionConfirmationEmail(
            String to,
            String candidateName,
            String interviewTitle,
            String position
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("covirateams@gmail.com");
        message.setTo(to);
        message.setSubject("We've received your interview submission");

        message.setText(
                "Hello " + candidateName + ",\n\n" +
                        "Thank you for completing your interview" +
                        (position != null && !position.isBlank() ? " for the " + position + " role" : "") +
                        (interviewTitle != null && !interviewTitle.isBlank() ? " (\"" + interviewTitle + "\")" : "") +
                        ".\n\n" +
                        "Your responses have been successfully recorded and are now under " +
                        "review by the employer.\n\n" +
                        "If you haven't heard back within 15 days, please consider your " +
                        "application unsuccessful for this role.\n\n" +
                        "Regards,\n" +
                        "The Covira Team"
        );

        mailSender.send(message);
    }

    // Sent to a candidate once the employer records a Shortlist or Reject
    // decision on their submission.
    public void sendCandidateDecisionEmail(
            String to,
            String candidateName,
            String interviewTitle,
            String position,
            String decision
    ) {

        boolean shortlisted = "SHORTLISTED".equalsIgnoreCase(decision);

        String roleText =
                (position != null && !position.isBlank())
                        ? " for the " + position + " role"
                        : "";

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("covirateams@gmail.com");
        message.setTo(to);

        if (shortlisted) {

            message.setSubject("You've been shortlisted" + roleText);

            message.setText(
                    "Hello " + candidateName + ",\n\n" +
                            "Good news.You have been shortlisted" + roleText +
                            (interviewTitle != null && !interviewTitle.isBlank()
                                    ? " after your submission for \"" + interviewTitle + "\""
                                    : "") +
                            ".\n\n" +
                            "The employer will be in touch with you about next steps.\n\n" +
                            "Regards,\n" +
                            "The Covira Team"
            );

        } else {

            message.setSubject("Update on your application" + roleText);

            message.setText(
                    "Hello " + candidateName + ",\n\n" +
                            "Thank you for taking the time to complete your interview" + roleText +
                            (interviewTitle != null && !interviewTitle.isBlank()
                                    ? " (\"" + interviewTitle + "\")"
                                    : "") +
                            ".\n\n" +
                            "After careful consideration, the employer has decided not to move " +
                            "forward with your application on this occasion.\n\n" +
                            "We wish you the best of luck with your job search.\n\n" +
                            "Regards,\n" +
                            "The Covira Team"
            );
            
        }
        

        mailSender.send(message);
    }
    public void sendCandidateSubmittedAlertEmail(
        String employerEmail,
        String ccEmail,
        String candidateName,
        String position,
        String interviewTitle
) {

    SimpleMailMessage message = new SimpleMailMessage();

    message.setFrom("covirateams@gmail.com");
    message.setTo(employerEmail);

    if (ccEmail != null && !ccEmail.isBlank()) {
        message.setCc(ccEmail);
    }

    String roleText =
            (position != null && !position.isBlank())
                    ? " for " + position
                    : "";

    message.setSubject("New candidate submission" + roleText);

    message.setText(
            "Hello,\n\n" +
                    candidateName + " has just completed the interview" +
                    (interviewTitle != null && !interviewTitle.isBlank()
                            ? " \"" + interviewTitle + "\""
                            : "") +
                    roleText +
                    ".\n\n" +
                    "Log in to Covira to review their responses:\n" +
                    "http://localhost:5173/login\n\n" +
                    "Regards,\n" +
                    "The Covira Team"
    );

    mailSender.send(message);
}
}