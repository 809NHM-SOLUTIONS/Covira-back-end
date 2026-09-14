package com.covira.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class InterviewCreateRequest {

    private String title;
    private String position;
    private String department;
    private String employmentType;
    private String location;
    private String description;
    private String employerEmail;

    private LocalDateTime candidateDeadline;

    private List<Long> questionIds;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }


    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }


    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getEmployerEmail() {
        return employerEmail;
    }

    public void setEmployerEmail(String employerEmail) {
        this.employerEmail = employerEmail;
    }


    public LocalDateTime getCandidateDeadline() {
        return candidateDeadline;
    }

    public void setCandidateDeadline(
            LocalDateTime candidateDeadline) {

        this.candidateDeadline = candidateDeadline;
    }


    public List<Long> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<Long> questionIds) {
        this.questionIds = questionIds;
    }
}