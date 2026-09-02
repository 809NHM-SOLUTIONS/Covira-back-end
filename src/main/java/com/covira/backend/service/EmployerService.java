package com.covira.backend.service;

import com.covira.backend.dto.ChangePasswordRequest;
import com.covira.backend.dto.EmployerProfileResponse;
import com.covira.backend.dto.EmployerProfileUpdateRequest;
import com.covira.backend.dto.EmployerSettingsResponse;
import com.covira.backend.dto.EmployerSettingsUpdateRequest;
import com.covira.backend.entity.User;
import com.covira.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public EmployerService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public EmployerProfileResponse getProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Employer not found."));

        return toProfileResponse(user);
    }

    public EmployerProfileResponse updateProfile(
            Long userId,
            EmployerProfileUpdateRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Employer not found."));

        user.setCompanyName(request.getCompanyName());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        userRepository.save(user);

        emailService.sendProfileUpdateConfirmationEmail(user.getEmail(), LocalDateTime.now());

        return toProfileResponse(user);
    }

    private EmployerProfileResponse toProfileResponse(User user) {
        return new EmployerProfileResponse(
                user.getCompanyName(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber()
        );
    }

    public String requestPasswordChange(Long userId, ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Employer not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (!isValidPassword(request.getNewPassword())) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character."
            );
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException(
                    "New password must be different from your current password."
            );
        }

        String otp = generateOtp();

        /* Hash the new password immediately. It is only ever copied into
         user.password once the OTP has been verified - never stored or
         transmitted in plain text.*/
        user.setPendingPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        emailService.sendChangePasswordOtpEmail(user.getEmail(), otp);

        return "OTP sent to your registered email address.";
    }

    public String verifyPasswordChangeOtp(Long userId, String otp) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Employer not found."));

        if (user.getPendingPassword() == null) {
            throw new IllegalArgumentException(
                    "No pending password change found. Please start again."
            );
        }

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid verification code.");
        }

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            clearPendingChange(user);
            userRepository.save(user);
            throw new IllegalArgumentException("This code has expired. Please request a new one.");
        }

        user.setPassword(user.getPendingPassword());
        clearPendingChange(user);

        userRepository.save(user);

        emailService.sendPasswordChangeConfirmationEmail(user.getEmail(), LocalDateTime.now());

        return "Your password has been changed successfully. Please log in again.";
    }

    public String resendPasswordChangeOtp(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Employer not found."));

        if (user.getPendingPassword() == null) {
            throw new IllegalArgumentException(
                    "No pending password change found. Please start again."
            );
        }

        String otp = generateOtp();

        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        emailService.sendChangePasswordOtpEmail(user.getEmail(), otp);

        return "A new OTP has been sent to your email.";
    }

    private void clearPendingChange(User user) {
        user.setPendingPassword(null);
        user.setOtp(null);
        user.setOtpExpiry(null);
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Password Policy:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one number
     * - At least one special character
     */
    private boolean isValidPassword(String password) {

        String regex =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&^#()_+=\\-]).{8,}$";

        return password != null && password.matches(regex);
    }

    public EmployerSettingsResponse getSettings(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Employer not found."));

        return toSettingsResponse(user);
    }

    public EmployerSettingsResponse updateSettings(
            Long userId,
            EmployerSettingsUpdateRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Employer not found."));

        user.setNotifyNewCandidateApplications(request.isNotifyNewCandidateApplications());
        user.setLanguage(request.getLanguage());
        user.setTimezone(request.getTimezone());

        userRepository.save(user);

        return toSettingsResponse(user);
    }

    private EmployerSettingsResponse toSettingsResponse(User user) {
        return new EmployerSettingsResponse(
                Boolean.TRUE.equals(user.getNotifyNewCandidateApplications()),
                user.getLanguage(),
                user.getTimezone()
        );
    }
}