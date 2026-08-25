package com.covira.backend.service;

import com.covira.backend.dto.InterviewCreateRequest;
import com.covira.backend.entity.Interview;
import com.covira.backend.entity.Question;
import com.covira.backend.repository.InterviewRepository;
import com.covira.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final QuestionRepository questionRepository;


    public InterviewService(
            InterviewRepository interviewRepository,
            QuestionRepository questionRepository) {

        this.interviewRepository =
                interviewRepository;

        this.questionRepository =
                questionRepository;
    }


    // =========================================================
    // CREATE INTERVIEW
    // =========================================================

    @Transactional
    public Interview createInterview(
            InterviewCreateRequest request) {

        if (request.getQuestionIds() == null ||
                request.getQuestionIds().isEmpty()) {

            throw new IllegalArgumentException(
                    "Please select at least one question."
            );
        }


        if (request.getEmployerEmail() == null ||
                request.getEmployerEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Employer email is required."
            );
        }


        Interview interview =
                new Interview();


        interview.setTitle(
                request.getTitle()
        );

        interview.setPosition(
                request.getPosition()
        );

        interview.setDepartment(
                request.getDepartment()
        );

        interview.setEmploymentType(
                request.getEmploymentType()
        );

        interview.setLocation(
                request.getLocation()
        );

        interview.setDescription(
                request.getDescription()
        );
        interview.setCandidateDeadline(
        request.getCandidateDeadline()
        );
        interview.setEmployerEmail(
                request.getEmployerEmail()
        );


        /*
         * Generate the unique token that will eventually
         * become the candidate's interview link.
         */
        interview.setAccessToken(
                UUID.randomUUID().toString()
        );


        interview.setStatus(
                "DRAFT"
        );


        interview.setCreatedAt(
                LocalDateTime.now()
        );


        // =====================================================
        // RETRIEVE ONLY THIS EMPLOYER'S QUESTIONS
        // =====================================================

        List<Question> questions =
                questionRepository
                        .findAllByIdInAndUserEmail(
                                request.getQuestionIds(),
                                request.getEmployerEmail()
                        );


        /*
         * Make sure every selected question belongs
         * to this employer.
         */
        if (questions.size() !=
                request.getQuestionIds().size()) {

            throw new IllegalArgumentException(
                    "One or more selected questions do not belong to this employer."
            );
        }


        // =====================================================
        // PRESERVE THE EMPLOYER'S SELECTED ORDER
        // =====================================================

        for (int i = 0;
             i < request.getQuestionIds().size();
             i++) {

            Long questionId =
                    request.getQuestionIds().get(i);


            Question selectedQuestion =
                    questions.stream()
                            .filter(question ->
                                    question.getId()
                                            .equals(questionId))
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Selected question could not be found."
                                    ));


            /*
             * Start order at 1 because this is easier
             * to understand when debugging and displaying
             * questions.
             */
            interview.addQuestion(
                    selectedQuestion,
                    i + 1
            );
        }


        return interviewRepository.save(
                interview
        );
    }


    // =========================================================
    // GET EMPLOYER'S INTERVIEWS
    // =========================================================

    public List<Interview> getEmployerInterviews(
            String employerEmail) {

        return interviewRepository
                .findByEmployerEmail(
                        employerEmail
                );
    }


    // =========================================================
    // GET INTERVIEW BY ID
    // =========================================================

    public Interview getInterviewById(
            Long id) {

        return interviewRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Interview not found"
                        ));
    }


    // =========================================================
    // GET INTERVIEW FOR CANDIDATE
    // =========================================================

    @Transactional(readOnly = true)
    public Interview getInterviewByToken(
            String token) {

        return interviewRepository
                .findByAccessToken(token)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Interview not found"
                        ));
    }


    // =========================================================
    // UPDATE INTERVIEW
    // =========================================================

    @Transactional
    public Interview updateInterview(
            Long id,
            InterviewCreateRequest request) {

        Interview interview =
                getInterviewById(id);


        interview.setTitle(
                request.getTitle()
        );

        interview.setPosition(
                request.getPosition()
        );

        interview.setDepartment(
                request.getDepartment()
        );

        interview.setEmploymentType(
                request.getEmploymentType()
        );

        interview.setLocation(
                request.getLocation()
        );

        interview.setDescription(
                request.getDescription()
        );

        interview.setCandidateDeadline(
               request.getCandidateDeadline()
        );
        // =====================================================
        // UPDATE QUESTIONS
        // =====================================================

        if (request.getQuestionIds() != null) {

            List<Question> questions =
                    questionRepository
                            .findAllByIdInAndUserEmail(
                                    request.getQuestionIds(),
                                    interview.getEmployerEmail()
                            );


            if (questions.size() !=
                    request.getQuestionIds().size()) {

                throw new IllegalArgumentException(
                        "One or more selected questions do not belong to this employer."
                );
            }


            interview.clearQuestions();


            for (int i = 0;
                 i < request.getQuestionIds().size();
                 i++) {

                Long questionId =
                        request.getQuestionIds().get(i);


                Question selectedQuestion =
                        questions.stream()
                                .filter(question ->
                                        question.getId()
                                                .equals(questionId))
                                .findFirst()
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Selected question could not be found."
                                        ));


                interview.addQuestion(
                        selectedQuestion,
                        i + 1
                );
            }
        }


        return interviewRepository.save(
                interview
        );
    }


    // =========================================================
    // PUBLISH INTERVIEW
    // =========================================================

    @Transactional
    public Interview publishInterview(
            Long id) {

        Interview interview =
                getInterviewById(id);


        if (interview.getInterviewQuestions() == null ||
                interview.getInterviewQuestions().isEmpty()) {

            throw new IllegalArgumentException(
                    "An interview must contain at least one question."
            );
        }


        interview.setStatus(
                "PUBLISHED"
        );


        return interviewRepository.save(
                interview
        );
    }


    // =========================================================
    // DELETE INTERVIEW
    // =========================================================

    @Transactional
    public void deleteInterview(
            Long id) {

        Interview interview =
                getInterviewById(id);

        interviewRepository.delete(
                interview
        );
    }
}