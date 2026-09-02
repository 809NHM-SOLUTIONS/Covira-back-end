package com.covira.backend.controller;

import com.covira.backend.dto.DashboardSummaryDto;
import com.covira.backend.service.DashboardService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(HttpSession session) {

        Long employerId = (Long) session.getAttribute("loggedInUserId");

        if (employerId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        return ResponseEntity.ok(dashboardService.getSummary(employerId));
    }
}