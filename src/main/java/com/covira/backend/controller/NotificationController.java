package com.covira.backend.controller;

import com.covira.backend.dto.NotificationDto;
import com.covira.backend.service.NotificationService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(HttpSession session) {

        Long employerId = (Long) session.getAttribute("loggedInUserId");

        if (employerId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        List<NotificationDto> notifications =
                notificationService.getNotifications(employerId);

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(HttpSession session) {

        Long employerId = (Long) session.getAttribute("loggedInUserId");

        if (employerId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        long count = notificationService.getUnreadCount(employerId);

        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            HttpSession session
    ) {

        Long employerId = (Long) session.getAttribute("loggedInUserId");

        if (employerId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        try {
            return ResponseEntity.ok(
                    notificationService.markAsRead(employerId, id)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}