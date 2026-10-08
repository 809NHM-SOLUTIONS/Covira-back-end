package com.covira.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.covira.backend.entity.Interview;
import com.covira.backend.entity.Question;
import com.covira.backend.repository.CandidateRepository;
import com.covira.backend.repository.InterviewRepository;
import com.covira.backend.repository.QuestionRepository;

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
            Integer timeLimit,
            List<String> options,
            Boolean allowMultiple,
            Boolean required
    ) {

        Interview interview = interviewRepository
                .findById(interviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Interview not found."
                        )
                );

        assertNotLocked(interview);

        List<String> cleanOptions = cleanOptions(questionType, options);

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
        question.setOptions(cleanOptions);
        question.setAllowMultiple(
                isMultipleChoice(questionType)
                        && Boolean.TRUE.equals(allowMultiple)
        );
        question.setQuestionOrder(existingQuestions.size() + 1);
        question.setRequired(!Boolean.FALSE.equals(required));

        return questionRepository.save(question);
    }

    public Question updateQuestion(
            Long interviewId,
            Long questionId,
            String questionText,
            String questionType,
            Integer timeLimit,
            List<String> options,
            Boolean allowMultiple,
            Boolean required
    ) {

        Interview interview = interviewRepository
                .findById(interviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Interview not found."
                        )
                );

        // Server-side lock: blocks edits once any candidate has submitted.
        assertNotLocked(interview);

        Question question = getQuestion(interviewId, questionId);

        List<String> cleanOptions = cleanOptions(questionType, options);

        question.setQuestionText(questionText);
        question.setQuestionType(questionType);
        question.setTimeLimit(timeLimit);
        question.setOptions(cleanOptions);
        question.setAllowMultiple(
                isMultipleChoice(questionType)
                        && Boolean.TRUE.equals(allowMultiple)
        );
        question.setRequired(!Boolean.FALSE.equals(required));

        return questionRepository.save(question);
    }

    public List<Question> getQuestions(Long interviewId) {

        return questionRepository
                .findAllByInterviewIdOrderByQuestionOrderAsc(
                        interviewId
                );
    }

    /**
     * An interview locks the moment any candidate has submitted it.
     *
     * Uses "has a submission time" rather than status "Completed", so the
     * lock stays in place after the employer shortlists or rejects the
     * candidate. Scoped to this employer so another employer's interview
     * with the same title cannot lock this one.
     */
    public boolean isLocked(Long interviewId) {

        Interview interview = interviewRepository
                .findById(interviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Interview not found."
                        )
                );

        return hasSubmissions(interview);
    }

    private boolean hasSubmissions(Interview interview) {

        return candidateRepository.hasSubmissions(
                interview.getEmployer().getId(),
                interview.getId(),
                interview.getTitle()
        );
    }

    private void assertNotLocked(Interview interview) {

        if (hasSubmissions(interview)) {
            throw new IllegalArgumentException(
                    "This interview has candidate submissions and can no longer be edited."
            );
        }
    }

    private boolean isMultipleChoice(String questionType) {
        return questionType != null
                && questionType.trim().equalsIgnoreCase("Multiple Choice");
    }

    /**
     * Trims, drops blanks, and requires 2+ options for multiple choice.
     * Other question types always get an empty list.
     */
    private List<String> cleanOptions(
            String questionType,
            List<String> options
    ) {

        if (!isMultipleChoice(questionType)) {
            return new ArrayList<>();
        }

        List<String> cleaned = options == null
                ? new ArrayList<>()
                : options.stream()
                        .filter(o -> o != null && !o.trim().isEmpty())
                        .map(String::trim)
                        .toList();

        if (cleaned.size() < 2) {
            throw new IllegalArgumentException(
                    "Multiple choice questions need at least 2 answer options."
            );
        }

        return cleaned;
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

        if (interview.isDeadlinePassed()) {
            throw new IllegalStateException(Interview.CLOSED_MESSAGE);
        }

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