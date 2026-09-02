package com.covira.backend.dto;

import java.time.LocalDateTime;

public class NotificationDto {

    private Long id;
    private String type;
    private String title;
    private String message;
    private Long candidateId;
    private String candidateName;
    private Long interviewId;
    private String interviewTitle;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationDto(
            Long id,
            String type,
            String title,
            String message,
            Long candidateId,
            String candidateName,
            Long interviewId,
            String interviewTitle,
            boolean read,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.interviewId = interviewId;
        this.interviewTitle = interviewTitle;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public Long getInterviewId() {
        return interviewId;
    }

    public String getInterviewTitle() {
        return interviewTitle;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}