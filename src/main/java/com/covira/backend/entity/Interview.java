package com.covira.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employer_id", nullable = false)
    @JsonIgnore
    private User employer;


    @Column(nullable = false)
    private String title;


    private String position;


    private String department;


    @Column(name = "employment_type")
    private String employmentType;


    private String location;


    private String description;

    /*
     * ============================================================
     * INTERVIEW SETTINGS
     * ============================================================
     *
     * All settings are nullable / wrapper types so that existing
     * rows (created before these columns existed) keep working:
     *   - answerTimeLimit  null      -> no limit
     *   - maxRetakes       null      -> unlimited retakes
     *   - candidateDeadline null     -> no deadline
     *   - requireConsent   null/false-> no consent checkbox
     *   - allowBackNavigation null/true -> back button allowed
     */

    public static final String CLOSED_MESSAGE =
            "The deadline for this interview has passed and it is "
                    + "no longer accepting responses. Please contact "
                    + "the employer if you need more time.";

    /** Maximum recording length per video answer, in seconds. */
    @Column(name = "answer_time_limit")
    private Integer answerTimeLimit;

    /** Re-records allowed per video question. 0 = none, null = unlimited. */
    @Column(name = "max_retakes")
    private Integer maxRetakes;

    /** Candidates can no longer open or start the interview after this. */
    @Column(name = "candidate_deadline")
    private LocalDateTime candidateDeadline;

    /** Optional intro shown to candidates before they begin. */
    @Column(name = "welcome_message", columnDefinition = "TEXT")
    private String welcomeMessage;

    /** Candidate must tick a recording/privacy consent box to start. */
    @Column(name = "require_consent")
    private Boolean requireConsent = false;

    /** Whether the candidate may go back to previous questions. */
    @Column(name = "allow_back_navigation")
    private Boolean allowBackNavigation = true;


    @Column(
            name = "interview_token",
            nullable = false,
            unique = true
    )
    private String interviewToken;


    @Column(nullable = false)
    private String status = "Active";


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    /*
     * ============================================================
     * QUESTIONS
     * ============================================================
     *
     * One interview can have many questions.
     *
     * This uses the same Question entity that is currently being
     * managed by QuestionController and QuestionService.
     */

    @OneToMany(
            mappedBy = "interview",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("questionOrder ASC")
    @JsonManagedReference
    private List<Question> questions = new ArrayList<>();


    public Interview() {
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


    public String getInterviewToken() {
        return interviewToken;
    }

    public void setInterviewToken(String interviewToken) {
        this.interviewToken = interviewToken;
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


    public Integer getAnswerTimeLimit() {
        return answerTimeLimit;
    }

    public void setAnswerTimeLimit(Integer answerTimeLimit) {
        this.answerTimeLimit = answerTimeLimit;
    }

    public Integer getMaxRetakes() {
        return maxRetakes;
    }

    public void setMaxRetakes(Integer maxRetakes) {
        this.maxRetakes = maxRetakes;
    }

    public LocalDateTime getCandidateDeadline() {
        return candidateDeadline;
    }

    public void setCandidateDeadline(LocalDateTime candidateDeadline) {
        this.candidateDeadline = candidateDeadline;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public Boolean getRequireConsent() {
        return requireConsent;
    }

    public void setRequireConsent(Boolean requireConsent) {
        this.requireConsent = requireConsent;
    }

    public Boolean getAllowBackNavigation() {
        return allowBackNavigation;
    }

    public void setAllowBackNavigation(Boolean allowBackNavigation) {
        this.allowBackNavigation = allowBackNavigation;
    }

    /**
     * True once the candidate deadline (if any) has passed.
     * Not serialised to JSON.
     */
    @JsonIgnore
    public boolean isDeadlinePassed() {
        return candidateDeadline != null
                && LocalDateTime.now().isAfter(candidateDeadline);
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }


    public void addQuestion(Question question) {

        questions.add(question);

        question.setInterview(this);
    }


    public void removeQuestion(Question question) {

        questions.remove(question);

        question.setInterview(null);
    }
}