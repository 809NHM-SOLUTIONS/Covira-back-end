package com.covira.backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String department;

    @Column(name = "employment_type", nullable = false)
    private String employmentType;

    @Column(nullable = false)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "employer_email", nullable = false)
    private String employerEmail;

    @Column(name = "access_token", nullable = false, unique = true)
    private String accessToken;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Candidate must complete the interview before this date/time.
    // This is optional.
    @Column(name = "candidate_deadline")
    private LocalDateTime candidateDeadline;

    @OneToMany(
            mappedBy = "interview",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("questionOrder ASC")
    @JsonManagedReference
    private List<InterviewQuestion> interviewQuestions =
            new ArrayList<>();


    public Interview() {
    }


    public Long getId() {
        return id;
    }


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


    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public LocalDateTime getCandidateDeadline() {
        return candidateDeadline;
    }

    public void setCandidateDeadline(LocalDateTime candidateDeadline) {
        this.candidateDeadline = candidateDeadline;
    }


    public List<InterviewQuestion> getInterviewQuestions() {
        return interviewQuestions;
    }


    public void setInterviewQuestions(
            List<InterviewQuestion> interviewQuestions) {

        this.interviewQuestions = interviewQuestions;
    }


    public void addQuestion(
            Question question,
            int questionOrder) {

        InterviewQuestion interviewQuestion =
                new InterviewQuestion();

        interviewQuestion.setInterview(this);
        interviewQuestion.setQuestion(question);
        interviewQuestion.setQuestionOrder(questionOrder);

        this.interviewQuestions.add(interviewQuestion);
    }


    public void clearQuestions() {
        this.interviewQuestions.clear();
    }
}