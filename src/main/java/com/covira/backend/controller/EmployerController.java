package com.covira.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.covira.backend.dto.ChangePasswordOtpRequest;
import com.covira.backend.dto.ChangePasswordRequest;
import com.covira.backend.dto.EmployerProfileResponse;
import com.covira.backend.dto.EmployerProfileUpdateRequest;
import com.covira.backend.service.EmployerService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employer")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class EmployerController {

    private final EmployerService employerService;

    public EmployerController(EmployerService employerService) {
        this.employerService = employerService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {

        Long userId = (Long) session.getAttribute("loggedInUserId");

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        EmployerProfileResponse response = employerService.getProfile(userId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody EmployerProfileUpdateRequest request,
            HttpSession session
    ) {

        Long userId = (Long) session.getAttribute("loggedInUserId");

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        EmployerProfileResponse response =
                employerService.updateProfile(userId, request);

        return ResponseEntity.ok(response);
    }

    /*
     * ============================================================
     * CHANGE PASSWORD (logged-in employer, OTP-verified)
     * ============================================================
     */

    @PostMapping("/profile/change-password/request")
    public ResponseEntity<String> requestPasswordChange(
            @RequestBody ChangePasswordRequest request,
            HttpSession session
    ) {

        Long userId = (Long) session.getAttribute("loggedInUserId");

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        try {
            return ResponseEntity.ok(
                    employerService.requestPasswordChange(userId, request)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/profile/change-password/verify")
    public ResponseEntity<String> verifyPasswordChange(
            @RequestBody ChangePasswordOtpRequest request,
            HttpSession session
    ) {

        Long userId = (Long) session.getAttribute("loggedInUserId");

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        try {
            String message = employerService.verifyPasswordChangeOtp(userId, request.getOtp());

            // Invalidate the session server-side too, so the old session
            // cookie can't keep authenticating after the password changed.
            session.invalidate();

            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/profile/change-password/resend")
    public ResponseEntity<String> resendPasswordChangeOtp(HttpSession session) {

        Long userId = (Long) session.getAttribute("loggedInUserId");

        if (userId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        try {
            return ResponseEntity.ok(
                    employerService.resendPasswordChangeOtp(userId)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}