package com.covira.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.covira.backend.dto.InterviewCreateRequest;
import com.covira.backend.entity.Interview;
import com.covira.backend.entity.User;
import com.covira.backend.repository.CandidateRepository;
import com.covira.backend.repository.InterviewRepository;
import com.covira.backend.repository.UserRepository;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;

    public InterviewService(
            InterviewRepository interviewRepository,
            UserRepository userRepository,
            CandidateRepository candidateRepository
    ) {
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
    }

    /*
     * ============================================================
     * CREATE INTERVIEW
     * ============================================================
     */

    public Interview createInterview(
            Long employerId,
            String title,
            String position,
            String department,
            String employmentType,
            String location,
            String description
    ) {

        User employer = userRepository
                .findById(employerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employer not found."
                        )
                );

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Interview title is required."
            );
        }

        Interview interview = new Interview();

        interview.setEmployer(employer);

        interview.setTitle(title.trim());

        interview.setPosition(position);

        interview.setDepartment(department);

        interview.setEmploymentType(employmentType);

        interview.setLocation(location);

        interview.setDescription(description);

        /*
         * Generate unique candidate interview token.
         */

        interview.setInterviewToken(
                UUID.randomUUID().toString()
        );


        /*
         * Interviews go live as soon as they are created.
         */

        interview.setStatus("Active");

        /*
         * Record creation time.
         */

        interview.setCreatedAt(
                LocalDateTime.now()
        );

        return interviewRepository.save(interview);
    }

    /*
     * ============================================================
     * CREATE INTERVIEW (WITH SETTINGS)
     * ============================================================
     *
     * Validates the interview settings, creates the interview and
     * stores the settings on it.
     */
    @Transactional
    public Interview createInterview(
            Long employerId,
            InterviewCreateRequest request
    ) {
        validateSettings(request);

        Interview interview = createInterview(
                employerId,
                request.getTitle(),
                request.getPosition(),
                request.getDepartment(),
                request.getEmploymentType(),
                request.getLocation(),
                request.getDescription()
        );

        interview.setAnswerTimeLimit(request.getAnswerTimeLimit());
        interview.setMaxRetakes(request.getMaxRetakes());
        interview.setCandidateDeadline(request.getCandidateDeadline());

        String welcome = request.getWelcomeMessage();
        interview.setWelcomeMessage(
                welcome == null || welcome.isBlank()
                        ? null
                        : welcome.trim()
        );

        interview.setRequireConsent(
                Boolean.TRUE.equals(request.getRequireConsent())
        );
        // Back navigation is allowed unless explicitly switched off.
        interview.setAllowBackNavigation(
                !Boolean.FALSE.equals(request.getAllowBackNavigation())
        );

        return interviewRepository.save(interview);
    }

    /*
     * Rejects out-of-range or nonsensical settings so that a
     * hand-crafted request cannot store bad values.
     */
    private void validateSettings(InterviewCreateRequest request) {
        Integer limit = request.getAnswerTimeLimit();
        if (limit != null && (limit < 10 || limit > 900)) {
            throw new IllegalArgumentException(
                    "Answer time limit must be between 10 and 900 seconds."
            );
        }

        Integer retakes = request.getMaxRetakes();
        if (retakes != null && (retakes < 0 || retakes > 10)) {
            throw new IllegalArgumentException(
                    "Retakes must be between 0 and 10, or unlimited."
            );
        }

        if (request.getCandidateDeadline() != null
                && !request.getCandidateDeadline()
                .isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "The candidate deadline must be in the future."
            );
        }

        String welcome = request.getWelcomeMessage();
        if (welcome != null && welcome.length() > 1000) {
            throw new IllegalArgumentException(
                    "The welcome message must be 1000 characters or fewer."
            );
        }
    }

    /*
     * ============================================================
     * GET ALL INTERVIEWS
     * ============================================================
     *
     * Returns all interviews belonging to the logged-in employer.
     *
     * GET:
     * /api/interviews
     *
     */

    public List<Interview> getAllInterviews(
            Long employerId
    ) {

        return interviewRepository
                .findAllByEmployerIdOrderByIdDesc(
                        employerId
                );
    }

    /*
     * ============================================================
     * GET ONE INTERVIEW
     * ============================================================
     *
     * Returns one interview belonging to the logged-in employer.
     *
     * GET:
     * /api/interviews/{id}
     *
     */

    public Interview getInterview(
            Long employerId,
            Long interviewId
    ) {

        return interviewRepository
                .findByIdAndEmployerId(
                        interviewId,
                        employerId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Interview not found."
                        )
                );
    }

    /*
     * ============================================================
     * GET INTERVIEW BY PUBLIC TOKEN
     * ============================================================
     *
     * Used by candidates.
     *
     * No employer login/session required.
     *
     * GET:
     * /api/interviews/public/{token}
     *
     */

    public Interview getInterviewByToken(
            String token
    ) {

        Interview interview = interviewRepository
                .findByInterviewToken(token)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Interview link is invalid or expired."
                        )
                );

        if (interview.isDeadlinePassed()) {
            throw new IllegalStateException(Interview.CLOSED_MESSAGE);
        }

        return interview;
    }

    /*
     * ============================================================
     * DELETE INTERVIEW
     * ============================================================
     *
     * Only the owning employer can delete an interview.
     *
     * An interview that already has submitted candidates cannot be
     * deleted - that would destroy responses the employer may still
     * need. Candidates who registered but never submitted are
     * removed together with the interview.
     *
     * DELETE:
     * /api/interviews/{id}
     *
     */

    @Transactional
    public void deleteInterview(
            Long employerId,
            Long interviewId
    ) {

        Interview interview = getInterview(employerId, interviewId);

        boolean hasSubmissions =
                candidateRepository.hasSubmissions(
                        employerId,
                        interview.getId(),
                        interview.getTitle()
                );

        if (hasSubmissions) {
            throw new IllegalArgumentException(
                    "This interview has candidate submissions and "
                            + "can't be deleted."
            );
        }

        // Candidates who opened the link but never submitted.
        candidateRepository.deleteAll(
                candidateRepository.findAllForInterview(
                        employerId,
                        interview.getId(),
                        interview.getTitle()
                )
        );

        // Questions are removed with the interview (cascade).
        interviewRepository.delete(interview);
    }
}