package com.covira.backend.dto;

public class EmployerSettingsResponse {

    private boolean notifyNewCandidateApplications;
    private String language;
    private String timezone;
    private String ccEmail;

    public EmployerSettingsResponse(
            boolean notifyNewCandidateApplications,
            String language,
            String timezone,
            String ccEmail
    ) {
        this.notifyNewCandidateApplications = notifyNewCandidateApplications;
        this.language = language;
        this.timezone = timezone;
        this.ccEmail = ccEmail;
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

    public String getCcEmail() {
        return ccEmail;
    }
}