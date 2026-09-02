package com.covira.backend.dto;

public class EmployerSettingsUpdateRequest {

    private boolean notifyNewCandidateApplications;
    private String language;
    private String timezone;

    public boolean isNotifyNewCandidateApplications() {
        return notifyNewCandidateApplications;
    }

    public void setNotifyNewCandidateApplications(boolean notifyNewCandidateApplications) {
        this.notifyNewCandidateApplications = notifyNewCandidateApplications;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}