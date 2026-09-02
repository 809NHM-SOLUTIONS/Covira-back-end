package com.covira.backend.dto;

public class ActionItemDto {

    private String type;
    private String title;
    private String message;
    private long count;

    private String linkType;

    private String severity;

    public ActionItemDto(
            String type,
            String title,
            String message,
            long count,
            String linkType,
            String severity
    ) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.count = count;
        this.linkType = linkType;
        this.severity = severity;
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

    public long getCount() {
        return count;
    }

    public String getLinkType() {
        return linkType;
    }

    public String getSeverity() {
        return severity;
    }
}