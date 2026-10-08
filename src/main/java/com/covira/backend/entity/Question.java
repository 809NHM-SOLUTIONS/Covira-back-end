package com.covira.backend.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "interview_id", nullable = false)
@JsonBackReference
private Interview interview;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false)
    private String questionType;

    private Integer timeLimit;

    @Column(nullable = false)
    private Integer questionOrder;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "question_options",
            joinColumns = @JoinColumn(name = "question_id")
    )
    @OrderColumn(name = "option_order")
    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private List<String> options = new ArrayList<>();

    /** Nullable on purpose so existing rows in PostgreSQL don't break. */
    private Boolean allowMultiple = false;

    private Boolean required = true;

    public Question() {
    }

    public Long getId() {
        return id;
    }

    public Interview getInterview() {
        return interview;
    }

    public void setInterview(Interview interview) {
        this.interview = interview;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }
        public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options.clear();
        if (options != null) {
            this.options.addAll(options);
        }
    }

    public boolean isAllowMultiple() {
        return Boolean.TRUE.equals(allowMultiple);
    }

    public void setAllowMultiple(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }
    public boolean isRequired() {
    return !Boolean.FALSE.equals(required);
}

public void setRequired(boolean required) {
    this.required = required;
}
}