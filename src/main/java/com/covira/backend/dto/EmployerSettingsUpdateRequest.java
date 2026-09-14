package com.covira.backend.dto;

public class EmployerSettingsUpdateRequest {

    private boolean notifyNewCandidateApplications;
    private String language;
    private String timezone;
    private String ccEmail;

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

    public String getCcEmail() {
        return ccEmail;
    }

    public void setCcEmail(String ccEmail) {
        this.ccEmail = ccEmail;
    }
}