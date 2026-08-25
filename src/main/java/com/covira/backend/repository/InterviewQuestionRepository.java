package com.covira.backend.repository;

import com.covira.backend.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewQuestionRepository
        extends JpaRepository<InterviewQuestion, Long> {

    List<InterviewQuestion> findByInterviewIdOrderByQuestionOrderAsc(
            Long interviewId
    );
}