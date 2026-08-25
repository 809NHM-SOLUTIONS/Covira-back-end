package com.covira.backend.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
@Entity
@Table(
        name = "interview_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"interview_id", "question_id"}
                )
        }
)
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(
        name = "interview_id",
        nullable = false
)
@JsonBackReference
private Interview interview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "question_id",
            nullable = false
    )
    private Question question;

    @Column(
            name = "question_order",
            nullable = false
    )
    private Integer questionOrder;


    public InterviewQuestion() {
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


    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }


    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }
}