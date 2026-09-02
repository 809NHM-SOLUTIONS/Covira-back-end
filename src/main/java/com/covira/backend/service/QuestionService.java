package com.covira.backend.service;

import com.covira.backend.entity.Interview;
import com.covira.backend.entity.Question;
import com.covira.backend.repository.CandidateRepository;
import com.covira.backend.repository.InterviewRepository;
import com.covira.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final InterviewRepository interviewRepository;
    private final CandidateRepository candidateRepository;

    public QuestionService(
            QuestionRepository questionRepository,
            InterviewRepository interviewRepository,
            CandidateRepository candidateRepository
    ) {
        this.questionRepository = questionRepository;
        this.interviewRepository = interviewRepository;
        this.candidateRepository = candidateRepository;
    }

    public Question createQuestion(
            Long interviewId,
            String questionText,
            String questionType,
            Integer timeLimit
    ) {

        Interview interview = interviewRepository
                .findById(interviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Interview not found."
                        )
                );

        assertNotLocked(interview);

        List<Question> existingQuestions =
                questionRepository
                        .findAllByInterviewIdOrderByQuestionOrderAsc(
                                interviewId
                        );

        Question question = new Question();

        question.setInterview(interview);
        question.setQuestionText(questionText);
        question.setQuestionType(questionType);
        question.setTimeLimit(timeLimit);

        question.setQuestionOrder(
                existingQuestions.size() + 1
        );

        return questionRepository.save(question);
    }

    public List<Question> getQuestions(Long interviewId) {

        return questionRepository
                .findAllByInterviewIdOrderByQuestionOrderAsc(
                        interviewId
                );
    }

    /**
      An interview locks the moment any candidate has completed it
     */
    public boolean isLocked(Long interviewId) {

        Interview interview = interviewRepository
                .findById(interviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Interview not found."
                        )
                );

        return candidateRepository.existsByInterviewAndStatus(
                interview.getTitle(),
                "Completed"
        );
    }

    private void assertNotLocked(Interview interview) {

        boolean hasCompletedCandidate =
                candidateRepository.existsByInterviewAndStatus(
                        interview.getTitle(),
                        "Completed"
                );

        if (hasCompletedCandidate) {
            throw new IllegalArgumentException(
                    "This interview has candidate submissions and can no longer be edited."
            );
        }
    }

    public List<Question> getPublicQuestions(String token) {

        Interview interview =
                interviewRepository
                        .findByInterviewToken(token)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Interview link is invalid or expired."
                                )
                        );

        return questionRepository
                .findAllByInterviewIdOrderByQuestionOrderAsc(
                        interview.getId()
                );
    }

    public Question getQuestion(
            Long interviewId,
            Long questionId
    ) {

        return questionRepository
                .findByIdAndInterviewId(
                        questionId,
                        interviewId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Question not found."
                        )
                );
    }

    public void deleteQuestion(
            Long interviewId,
            Long questionId
    ) {

        Interview interview = interviewRepository
                .findById(interviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Interview not found."
                        )
                );

        assertNotLocked(interview);

        Question question = getQuestion(
                interviewId,
                questionId
        );

        questionRepository.delete(question);
    }
}