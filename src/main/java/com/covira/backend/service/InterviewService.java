package com.covira.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.covira.backend.dto.InterviewCreateRequest;
import com.covira.backend.entity.Candidate;
import com.covira.backend.entity.CandidateResponse;
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
        validateSettings(request, null);

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
    private void validateSettings(
            InterviewCreateRequest request,
            LocalDateTime existingDeadline
    ) {
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

        // A deadline that is unchanged from what is already stored is
        // accepted even if it has since passed, so other fields can
        // still be edited. A new or changed deadline must be in the future.
        if (request.getCandidateDeadline() != null
                && !request.getCandidateDeadline().equals(existingDeadline)
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
     * UPDATE INTERVIEW
     * ============================================================
     *
     * Only the owning employer can edit an interview, and only
     * while no candidate has submitted it - the same rule that
     * locks its questions.
     *
     * PUT:
     * /api/interviews/{id}
     */

    @Transactional
    public Interview updateInterview(
            Long employerId,
            Long interviewId,
            InterviewCreateRequest request
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
                            + "can no longer be edited."
            );
        }

        if (request.getTitle() == null
                || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Interview title is required."
            );
        }

        validateSettings(request, interview.getCandidateDeadline());

        String oldTitle = interview.getTitle();

        // Candidates who opened the link but never submitted carry a
        // copy of the interview details - keep them in step.
        List<Candidate> candidates =
                candidateRepository.findAllForInterview(
                        employerId,
                        interview.getId(),
                        oldTitle
                );

        interview.setTitle(request.getTitle().trim());
        interview.setPosition(request.getPosition());
        interview.setDepartment(request.getDepartment());
        interview.setEmploymentType(request.getEmploymentType());
        interview.setLocation(request.getLocation());
        interview.setDescription(request.getDescription());

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
        interview.setAllowBackNavigation(
                !Boolean.FALSE.equals(request.getAllowBackNavigation())
        );

        for (Candidate candidate : candidates) {
            candidate.setInterview(interview.getTitle());
            candidate.setInterviewId(interview.getId());
            candidate.setPosition(interview.getPosition());
            candidate.setDepartment(interview.getDepartment());
            candidate.setLocation(interview.getLocation());
        }
        candidateRepository.saveAll(candidates);

        return interviewRepository.save(interview);
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

        if (interview.isArchived()) {
            throw new IllegalStateException(Interview.ARCHIVED_MESSAGE);
        }

        if (interview.isDeadlinePassed()) {
            throw new IllegalStateException(Interview.CLOSED_MESSAGE);
        }

        return interview;
    }

    /*
     * ============================================================
     * ARCHIVE / RESTORE INTERVIEW
     * ============================================================
     *
     * Archiving closes the candidate link and moves the interview
     * out of the main list. Nothing is deleted, and it can be
     * restored at any time.
     *
     * POST /api/interviews/{id}/archive
     * POST /api/interviews/{id}/restore
     */

    @Transactional
    public Interview archiveInterview(
            Long employerId,
            Long interviewId
    ) {

        Interview interview = getInterview(employerId, interviewId);

        interview.setStatus(Interview.ARCHIVED_STATUS);

        return interviewRepository.save(interview);
    }

    @Transactional
    public Interview restoreInterview(
            Long employerId,
            Long interviewId
    ) {

        Interview interview = getInterview(employerId, interviewId);

        interview.setStatus("Active");

        return interviewRepository.save(interview);
    }

    /*
     * ============================================================
     * DELETE INTERVIEW
     * ============================================================
     *
     * Permanently deletes the interview, its questions, its
     * candidates, their answers and their uploaded videos.
     *
     * Refused while any candidate has submitted but has not yet
     * been shortlisted or rejected - the employer has to finish
     * reviewing first. Candidates who registered but never
     * submitted are removed with the interview.
     *
     * DELETE:
     * /api/interviews/{id}
     */

    @Transactional
    public void deleteInterview(
            Long employerId,
            Long interviewId
    ) {

        Interview interview = getInterview(employerId, interviewId);

        List<Candidate> candidates =
                candidateRepository.findAllForInterview(
                        employerId,
                        interview.getId(),
                        interview.getTitle()
                );

        long awaitingReview = candidates.stream()
                .filter(c -> c.getSubmittedAt() != null
                        && "Completed".equalsIgnoreCase(c.getStatus()))
                .count();

        if (awaitingReview > 0) {
            throw new IllegalArgumentException(
                    awaitingReview
                            + (awaitingReview == 1
                                    ? " candidate is"
                                    : " candidates are")
                            + " still awaiting review. Shortlist or "
                            + "reject them before deleting this interview."
            );
        }

        // Remember the video files; they are removed only once the
        // database delete has actually been committed.
        List<Path> videoFiles = new ArrayList<>();

        for (Candidate candidate : candidates) {
            for (CandidateResponse response : candidate.getResponses()) {
                Path file = resolveUploadedVideo(response.getVideoUrl());
                if (file != null) {
                    videoFiles.add(file);
                }
            }
        }

        candidateRepository.deleteAll(candidates);

        // Questions are removed with the interview (cascade).
        interviewRepository.delete(interview);

        if (videoFiles.isEmpty()) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            deleteFiles(videoFiles);
                        }
                    }
            );
        } else {
            deleteFiles(videoFiles);
        }
    }

    /*
     * Maps a stored "/uploads/candidate-responses/<file>" URL to the
     * file on disk. Returns null for anything that is not a plain file
     * inside that folder.
     */
    private Path resolveUploadedVideo(String videoUrl) {

        String prefix = "/uploads/candidate-responses/";

        if (videoUrl == null || !videoUrl.startsWith(prefix)) {
            return null;
        }

        String name = videoUrl.substring(prefix.length());

        if (name.isBlank()
                || name.contains("/")
                || name.contains("\\")
                || name.contains("..")) {
            return null;
        }

        Path folder = Paths.get("uploads", "candidate-responses")
                .toAbsolutePath()
                .normalize();

        Path file = folder.resolve(name).normalize();

        return file.startsWith(folder) ? file : null;
    }

    private void deleteFiles(List<Path> files) {

        for (Path file : files) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                // The interview is already gone; a leftover file is
                // not worth failing the request for.
                System.err.println(
                        "Could not delete video file " + file + ": "
                                + e.getMessage()
                );
            }
        }
    }
}