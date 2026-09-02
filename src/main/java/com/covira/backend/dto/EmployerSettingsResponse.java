package com.covira.backend.dto;

public class EmployerSettingsResponse {

    private boolean notifyNewCandidateApplications;
    private String language;
    private String timezone;

    public EmployerSettingsResponse(
            boolean notifyNewCandidateApplications,
            String language,
            String timezone
    ) {
        this.notifyNewCandidateApplications = notifyNewCandidateApplications;
        this.language = language;
        this.timezone = timezone;
    }

    public boolean isNotifyNewCandidateApplications() {
        return notifyNewCandidateApplications;
    }

    public String getLanguage() {
        return language;
    }

    public String getTimezone() {
        return timezone;
    }
}