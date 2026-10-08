package com.covira.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String position;

    /*
     * Interview title, kept for display. Older candidates were linked
     * to their interview by this title only.
     */
    private String interview;

    /*
     * ID of the interview this candidate belongs to. Null for older
     * candidates created before this column existed.
     */
    @Column(name = "interview_id")
    private Long interviewId;

    private String department;
    private String location;
    private String status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @OneToMany(
            mappedBy = "candidate",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("questionNumber ASC")
    private List<CandidateResponse> responses = new ArrayList<>();

    public Candidate() {
    }

    public Long getId() {
        return id;
    }

    public User getEmployer() {
        return employer;
    }

    public void setEmployer(User employer) {
        this.employer = employer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getInterview() {
        return interview;
    }

    public void setInterview(String interview) {
        this.interview = interview;
    }

    public Long getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(Long interviewId) {
        this.interviewId = interviewId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<CandidateResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<CandidateResponse> responses) {
        this.responses = responses;
    }

    public void addResponse(CandidateResponse response) {
        responses.add(response);
        response.setCandidate(this);
    }
}