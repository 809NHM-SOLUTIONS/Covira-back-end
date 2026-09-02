package com.covira.backend.dto;

import java.time.LocalDateTime;

public class RecentInterviewDto {

    private Long id;
    private String title;
    private String position;
    private String status;
    private int candidateCount;
    private int questionCount;
    private int completedResponseCount;
    private LocalDateTime createdAt;

    public RecentInterviewDto(
            Long id,
            String title,
            String position,
            String status,
            int candidateCount,
            int questionCount,
            int completedResponseCount,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.title = title;
        this.position = position;
        this.status = status;
        this.candidateCount = candidateCount;
        this.questionCount = questionCount;
        this.completedResponseCount = completedResponseCount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPosition() {
        return position;
    }

    public String getStatus() {
        return status;
    }

    public int getCandidateCount() {
        return candidateCount;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public int getCompletedResponseCount() {
        return completedResponseCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}