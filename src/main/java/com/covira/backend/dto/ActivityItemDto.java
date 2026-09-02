package com.covira.backend.dto;

import java.time.LocalDateTime;

public class ActivityItemDto {

    private String type;
    private String message;
    private LocalDateTime timestamp;
    private Long interviewId;

    public ActivityItemDto(
            String type,
            String message,
            LocalDateTime timestamp,
            Long interviewId
    ) {
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.interviewId = interviewId;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Long getInterviewId() {
        return interviewId;
    }
}